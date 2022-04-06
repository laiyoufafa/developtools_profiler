import * as path from 'path';

const profilerServicePath = path.join(__dirname,'../proto', 'profiler_service.proto');

export class ProfilerClient {
        // proto filePaths
        private _filePaths: Array<String> | undefined;
        // client
        private _client: any;
        // profiler_proto
        private _profiler_proto: any;
        // ProfilerClient constructor
        public constructor(address: Address) {
                // load client port
                let clientPort = this.loadAddress(address);
                // load proto file
                this.start(clientPort, profilerServicePath);
        };

        get filePaths(): Array<String> | undefined {
                return this._filePaths;
        }

        set filePaths(value: Array<String> | undefined) {
                this._filePaths = value;
        }

        get client(): any {
                return this._client;
        }

        set client(value: any) {
                this._client = value;
        }

        get profiler_proto(): any {
                return this._profiler_proto;
        }

        set profiler_proto(value: any) {
                this._profiler_proto = value;
        }

        start(address: string, filePath: string){
                // let loadPackage = proto_load.loadSync(
                //     filePath,
                //     {
                //             keepCase: true,
                //             longs: String,
                //             enums: String,
                //             defaults: true,
                //             oneofs: true
                //     }
                // );
                // // profiler Proto
                // this._profiler_proto = rpc.loadPackageDefinition(loadPackage);
                // // get profilerProto service
                // let profilerProto = this._profiler_proto.profiler;
                // // client
                // this._client = new profilerProto.IProfilerService('127.0.0.1:5555', rpc.credentials.createInsecure());
        }

        // Address
        loadAddress(clientAddress: Address): string{
                return clientAddress.host + ':' + clientAddress.port;
        };

        public getProfilerClient(callback: any): any{
                return this._client;
        };

        public getCapabilities(callback: any) {
                this._client.
                this._client.getCapabilities(callback);
                callback();
        };

        public createSession(callback: any) {
                this._client.createSession(callback);
                callback();
        };

        public startSession(callback: any) {
                this._client.startSession(callback);
                callback();
        };

        public stopSession(callback: any) {
                this._client.stopSession(callback);
                callback();
        };

        public destroySession(callback: any) {
                this._client.destroySession(callback);
                callback();
        };

        public keepSession(callback: any) {
                this._client.keepSession(callback);
                callback();
        };

        public shutdown(): void {

        };

        public getChannel() {
                return this._client.channelInterpretation;
        };

}

export interface Address {
        // port
        port: string | number;

        // host
        host?: string | number;
}
