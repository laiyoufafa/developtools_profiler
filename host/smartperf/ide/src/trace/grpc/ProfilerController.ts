export class ClientContainer {
    // private _credentials: rpc.ChannelCredentials | undefined;
    // private _clients: { service: any, client?: rpc.Client, target: any }[] = [];
    private _port: string | number | undefined;
    private _host: string | undefined;

   /* get clients(): { service: any; client?: rpc.Client; target: any }[] {
        return this._clients;
    }

    set clients(value: { service: any; client?: rpc.Client; target: any }[]) {
        this._clients = value;
    }*/

   /* get credentials(): rpc.ChannelCredentials | undefined {
        return this._credentials;
    }

    set credentials(value: rpc.ChannelCredentials | undefined) {
        this._credentials = value;
    }*/

    get port(): string | number | undefined {
        return this._port;
    }

    set port(value: string | number | undefined) {
        this._port = value;
    }

    get host(): string | undefined {
        return this._host;
    }

    set host(value: string | undefined) {
        this._host = value;
    }

    public registryClient(target: any, path: string) {
        // let packageDefinition = proto_load.loadSync(path, {
        //     keepCase: true,
        //     longs: String,
        //     enums: String,
        //     defaults: true,
        //     oneofs: true
        // });
        // let protoDescriptor = rpc.loadPackageDefinition(packageDefinition);
        //
        // const packages = Object.keys(protoDescriptor);
        // for (let packageKey of packages) {
        //     for (let key in protoDescriptor[packageKey]) {
        //
        //     }
        // }
    };

    public start() {
        this.loadSettings();
        this._registryClient();
    }

    private loadSettings() {
        let { host, port} = SettingRegistry.settings;
        this._host = host;
        this._port = port;
    }

    private _registryClient() {
        // for (let clientContainer of this._clients) {
        //     let client: rpc.Client = new clientContainer.service(
        //         `${this.host}:${this.port}`,
        //         this.credentials
        //     );
        //     clientContainer.client = client;
        // }
    }
}


export class SettingRegistry {
    static settings: Settings;

    static registry(settings: Settings) {
        this.settings = settings;
    }
}

export interface Settings {
    port: string | number;

    host?: string;
}