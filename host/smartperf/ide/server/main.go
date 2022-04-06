package main

//遇到报错请在当前目录下执行这个命令： go mod download golang.org/x/text
import (
    "bufio"
    "crypto/rand"
    "crypto/rsa"
    "crypto/x509"
    "crypto/x509/pkix"
    "encoding/pem"
    "math/big"
    "net"
    "bytes"
    "fmt"
    "io"
    "io/ioutil"
    "log"
    "mime"
    "net/http"
    "os"
    "os/exec"
    "path"
    "path/filepath"
    "runtime"
    "strings"
    "time"
)

const HttpPort = 9001

var exPath string

// CGO_ENABLED=0 GOOS=windows GOARCH=amd64 go build main.go
// CGO_ENABLED=0 GOOS=darwin GOARCH=amd64 go build main.go
func cors(fs http.Handler, version string) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
		// return if you do not want the FileServer handle a specific request
        r.Header.Add("Cross-Origin-Opener-Policy", "same-origin")
        r.Header.Add("Cross-Origin-Embedder-Policy", "require-corp")
        w.Header().Add("Cross-Origin-Opener-Policy", "same-origin")
        w.Header().Add("Cross-Origin-Embedder-Policy", "require-corp")
        w.Header().Set("Access-Control-Allow-Origin", "*")
        w.Header().Set("Access-Control-Allow-Credentials", "true")
        w.Header().Set("Access-Control-Allow-Headers", "x-requested-with, authorization, blade-auth") //*
        w.Header().Set("Access-Control-Allow-Methods", "*")                                           //*
        w.Header().Set("Access-Control-Max-Age", "3600")
        w.Header().Set("data-version", version)
        fs.ServeHTTP(w, r)
    }
}

func exist(path string) bool {
    _, err := os.Stat(path)
    if err != nil {
        if os.IsExist(err) {
            return true
        }
        return false
    }
    return true
}
func genSSL() {
    if exist("cert/keyFile.key") || exist("cert/certFile.pem") {
        fmt.Println("keyFile.key exists")
        return
    }
    max := new(big.Int).Lsh(big.NewInt(1), 128)
    serialNumber, _ := rand.Int(rand.Reader, max)
    subject := pkix.Name{
        Organization:       []string{"www.smartperf.com"},
        OrganizationalUnit: []string{"ITs"},
        CommonName:         "www.smartperf.com",
	}
    certificate509 := x509.Certificate{
        SerialNumber: serialNumber,
        Subject:      subject,
        NotBefore:    time.Now(),
        NotAfter:     time.Now().AddDate(10, 0, 0),
        KeyUsage:     x509.KeyUsageKeyEncipherment | x509.KeyUsageDigitalSignature,
        ExtKeyUsage:  []x509.ExtKeyUsage{x509.ExtKeyUsageServerAuth},
        IPAddresses:  []net.IP{net.ParseIP("127.0.0.1")},
    }
    chekDir("cert")
    pk, _ := rsa.GenerateKey(rand.Reader, 1024)
    derBytes, _ := x509.CreateCertificate(rand.Reader, &certificate509, &certificate509, &pk.PublicKey, pk)
    certOut, _ := os.Create("cert/certFile.pem")
    pem.Encode(certOut, &pem.Block{Type: "CERTIFICATE", Bytes: derBytes})
    certOut.Close()
    keyOut, _ := os.Create("cert/keyFile.key")
    pem.Encode(keyOut, &pem.Block{Type: "RSA PRIVATE KEY", Bytes: x509.MarshalPKCS1PrivateKey(pk)})
    keyOut.Close()
}
func main() {
    genSSL()
    exPath = getCurrentAbPath()
    fmt.Println(exPath)
    go func() {
        version := ""
        readVersion, versionErr := ioutil.ReadFile(exPath + "/version.txt")
        if versionErr != nil {
            version = ""
        } else {
            version = string(readVersion)
        }
        mux := http.NewServeMux()
        mime.TypeByExtension(".js")
        mime.AddExtensionType(".js", "application/javascript")
        log.Println(mime.TypeByExtension(".js"))
        mux.HandleFunc("/upload", uploadHandler)
        mux.Handle("/upload/", http.StripPrefix("/upload/", http.FileServer(http.Dir(exPath+"/upload"))))
        fs := http.FileServer(http.Dir(exPath + "/"))
        mux.Handle("/application/", http.StripPrefix("/application/", cors(fs, version)))
        ser := &http.Server{
            Addr:    fmt.Sprintf(":%d", HttpPort),
            Handler: mux,
        }
        log.Println(fmt.Sprintf("HTTP[%d]服务启动", HttpPort))
        open(fmt.Sprintf("https://localhost:%d/application", HttpPort))
        err := ser.ListenAndServeTLS("cert/certFile.pem", "cert/keyFile.key")
        CheckErr(err)
    }()
    select {}
}

func uploadHandler(w http.ResponseWriter, r *http.Request) {
    defer func() {
        var err = recover()
        fmt.Println(err)
    }()
    chekDir(exPath + "/upload")
    contentType := r.Header["Content-Type"]
    if len(contentType) > 0 {
        contentTypeName := contentType[0]
        if strings.HasPrefix(contentTypeName, "multipart/form-data") {
            err := r.ParseMultipartForm(32 << 20)
            CheckErr(err)
            file, header, err := r.FormFile("file")
            CheckErr(err)
            filename := header.Filename
            index := strings.LastIndex(filename, ".")
            distFileName := fmt.Sprintf("%d", time.Now().Unix())
            distFileSuffix := filename[index:]
            path := fmt.Sprintf("/upload/%s%s", distFileName, distFileSuffix)
            dst, err := os.OpenFile(exPath+path, os.O_WRONLY|os.O_CREATE, 0666)
            CheckErr(err)
            defer dst.Close()
            if _, err := io.Copy(dst, file); err != nil {
                http.Error(w, err.Error(), http.StatusInternalServerError)
                return
            }
            databaseUrl := transformDatabase(distFileName, distFileSuffix)
            if databaseUrl != "" {
                ohosTsPath := fmt.Sprintf("./upload/%s.ohos.ts", databaseUrl)
                result, _ := PathExists(ohosTsPath)
                if result {
                    readFile, readErr := ioutil.ReadFile(ohosTsPath)
                    if readErr == nil {
                        fmt.Println(string(readFile))
                        split := SplitLines(string(readFile))
                        fmt.Println(split)
                        if len(split) > 1 {
                            if strings.HasSuffix(split[0], ":0") {
                                fmt.Fprintf(w, fmt.Sprintf("/upload/%s", databaseUrl))
                                return
                            }
                        }
                    }
                }
            }
            http.Error(w, "文件生成失败", http.StatusNotFound)
            return
        }
    }
}
func SplitLines(s string) []string {
    var lines []string
    sc := bufio.NewScanner(strings.NewReader(s))
    for sc.Scan() {
        lines = append(lines, sc.Text())
    }
    return lines
}

func readFileFirstLine(path string) string {
    file, err := os.Open(path)
    if err != nil {
        return ""
    }
    defer file.Close()

    readFile := bufio.NewReader(file)
    line, readErr := readFile.ReadString('\n')
    if readErr != nil || io.EOF == err {
        return ""
    }
    return line
}

func PathExists(path string) (bool, error) {
    _, err := os.Stat(path)
    if err == nil {
        return true, nil
    }
    if os.IsNotExist(err) {
        return false, nil
    }
    return false, err
}

func chekDir(path string) {
    _, err := os.Stat(path)
    if err != nil {
        err := os.Mkdir(path, os.ModePerm)
        if err != nil {
            fmt.Printf("mkdir failed![%v]\n", err)
        } else {
            fmt.Printf("mkdir success!\n")
        }
    }
}
func CheckErr(err error) {
    if err != nil {
        log.Panicln(err)
    }
}

func open(url string) error {
    if isWindows() {
        return openUrlWindows(url)
    } else if isDarwin() {
        return openUrlDarwin(url)
    } else {
        return openUrlOther(url)
    }
}

func openUrlWindows(url string) error {
    cmd := "cmd"
    args := []string{"/c", "start", url}
    return exec.Command(cmd, args...).Start()
}
func openUrlDarwin(url string) error {
    var cmd = "open"
    var args = []string{url}
    return exec.Command(cmd, args...).Start()
}
func openUrlOther(url string) error {
    var cmd = "xdg-open"
    var args = []string{url}
    return exec.Command(cmd, args...).Start()
}

func isWindows() bool {
    return runtime.GOOS == "windows"
}
func isDarwin() bool {
    return runtime.GOOS == "darwin"
}

func transformDatabase(name string, suffix string) string {
    if isWindows() {
        cmd := exec.Command(
            "cmd",
            "/c",
            `.\bin\trace_streamer_windows.exe`,
            fmt.Sprintf(`.\upload\%s%s`, name, suffix),
            "-e",
            fmt.Sprintf(`.\upload\%s.db`, name),
        )
        var stdout, stderr bytes.Buffer
        cmd.Stdout = &stdout // 标准输出
        cmd.Stderr = &stderr // 标准错误
        err := cmd.Run()
        if err != nil {
            return ""
        }
        return fmt.Sprintf("%s.db", name)
    } else if isDarwin() {
        cmd := exec.Command(
            "/bin/bash",
            "-c",
            fmt.Sprintf("%s/bin/trace_streamer_mac %s/upload/%s%s -e %s/upload/%s.db", exPath, exPath, name, suffix, exPath, name),
        )
        var stdout, stderr bytes.Buffer
        cmd.Stdout = &stdout // 标准输出
        cmd.Stderr = &stderr // 标准错误
        err := cmd.Run()
        outStr, errStr := string(stdout.Bytes()), string(stderr.Bytes())
        fmt.Printf("out:\n%s\n :\n%s\n", outStr, errStr)
        if err != nil {
            return ""
        }
        return fmt.Sprintf("%s.db", name)
    } else {
        cmd := exec.Command(
            "/bin/bash",
            "-c",
            fmt.Sprintf("%s/bin/trace_streamer_linux %s/upload/%s%s -e %s/upload/%s.db", exPath, exPath, name, suffix, exPath, name),
        )
        var stdout, stderr bytes.Buffer
        cmd.Stdout = &stdout // 标准输出
        cmd.Stderr = &stderr // 标准错误
        err := cmd.Run()
        outStr, errStr := string(stdout.Bytes()), string(stderr.Bytes())
        fmt.Printf("out:\n%s\n :\n%s\n", outStr, errStr)
        if err != nil {
            return ""
        }
        return fmt.Sprintf("%s.db", name)
    }
    return ""
}
func getCurrentAbPath() string {
    dir := getCurrentAbPathByExecutable()
    tmpDir, _ := filepath.EvalSymlinks(os.TempDir())
    if strings.Contains(dir, tmpDir) {
        return getCurrentAbPathByCaller()
    }
    return dir
}

func getCurrentAbPathByCaller() string {
    var abPath string
    _, filename, _, ok := runtime.Caller(0)
    if ok {
        abPath = path.Dir(filename)
    }
    return abPath
}
func getCurrentAbPathByExecutable() string {
    exePath, err := os.Executable()
    if err != nil {
        log.Fatal(err)
    }
    res, _ := filepath.EvalSymlinks(filepath.Dir(exePath))
    return res
}
