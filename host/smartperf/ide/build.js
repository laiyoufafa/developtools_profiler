const path = require('path');
const fs = require("fs");
const child_process = require("child_process");
const os = require("os");

const compileServer = true
const outDir = "dist"

const staticPath = [
    "/src/img",
    "/server/cert",
]

const staticFiles = [
    "/server/version.txt",
    "/src/index.html",
    "/src/base-ui/icon.svg"
]

const thirdParty = [
    {srcFilePath: "/third-party/sql-wasm.wasm", distFilePath:"/trace/database/sql-wasm.wasm" },
    {srcFilePath: "/third-party/sql-wasm.js", distFilePath:"/trace/database/sql-wasm.js" },
    {srcFilePath: "/third-party/worker.sql-wasm.js", distFilePath:"/trace/database/worker.sql-wasm.js"}
]

function cpFile(from, to) {
     fs.writeFileSync(to, fs.readFileSync(from))
}

function main() {
    // clean outDir
    let outPath = path.normalize(path.join(__dirname, "/", outDir));
    if (checkDirExist(outPath)) {
        removeDir(outPath)
    }
    // run tsc compile
    let rootPath = path.join(__dirname,"/");
    child_process.execSync("tsc -p "+ rootPath)
    // run cp to mv all staticFile
    staticFiles.forEach(value => {
        let filePath = path.join(__dirname, value)
        let distFile;
        if(value.startsWith("/src")) {
            distFile = path.join(__dirname, outDir, value.substring(4, value.length + 1))
        } else if (value.startsWith("/server")){
            distFile = path.join(__dirname, outDir, value.substring(7, value.length + 1))
        }
        cpFile(filePath, distFile);
    })
    staticPath.forEach(value => {
        let pa = path.join(__dirname, value)
        let distPath;
        if(value.startsWith("/src")) {
            distPath = path.join(__dirname, outDir, value.substring(4, value.length + 1))
        } else if (value.startsWith("/server")){
            distPath = path.join(__dirname, outDir, value.substring(7, value.length + 1))
        }
        copyDirectory(pa, distPath);
    })
    thirdParty.forEach(value => {
        let thirdFile = path.join(__dirname, value.srcFilePath)
        let thirdDistFile = path.join(__dirname, outDir,  value.distFilePath)
        cpFile(thirdFile, thirdDistFile);
    })
    let traceStreamer = path.normalize(path.join(__dirname, "/bin"));
    if (checkDirExist(traceStreamer)) {
        let dest = path.normalize(path.join(__dirname, outDir, "/bin"));
        copyDirectory(traceStreamer, dest)
        // to mv traceStream Wasm and js
        cpFile(traceStreamer + "/trace_streamer_builtin.js", rootPath + outDir +"/trace/database/trace_streamer_builtin.js")
        cpFile(traceStreamer + "/trace_streamer_builtin.wasm", rootPath + outDir + "/trace/database/trace_streamer_builtin.wasm")
    } else {
        throw new Error("traceStreamer dir is Not Exits")
    }
    // compile server
    if (compileServer) {
        let serverSrc = path.normalize(path.join(__dirname, "/server/main.go"));
        if (os.type() === "Windows_NT") {
            child_process.spawnSync("go", ["build", "-o", outPath, serverSrc])
        } else if (os.type() == "Darwin"){
             child_process.spawnSync("go", ["build", "-o", outPath + "/main", serverSrc])
        } else {
            child_process.spawnSync("go", ["build", "-o", outPath + "/main", serverSrc])
        }
    }
}


function copyDirectory(src, dest) {
    if (checkDirExist(dest) == false) {
        fs.mkdirSync(dest);
    }
    if (checkDirExist(src) == false) {
        return false;
    }
    let directories = fs.readdirSync(src);
    directories.forEach((value) =>{
        let filePath = path.join(src, value);
        let fileSys = fs.statSync(filePath);
        if (fileSys.isFile()) {
            fs.copyFileSync(filePath, path.join(dest, value));
        } else if (fileSys.isDirectory()){
            copyDirectory(filePath, path.join(dest, value));
        }
    });
}

function checkDirExist(dirPath) {
    return fs.existsSync(dirPath)
}

function removeDir(outPath) {
    let files = [];
    if(fs.existsSync(outPath)){
        files = fs.readdirSync(outPath);
        files.forEach((file, index) => {
            let curPath = outPath + "/" + file;
            if(fs.statSync(curPath).isDirectory()){
                removeDir(curPath);
            } else {
                fs.unlinkSync(curPath);
            }
        });
        fs.rmdirSync(outPath);
    }
}
main();