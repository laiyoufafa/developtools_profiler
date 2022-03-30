import {Address, ProfilerClient} from "./ProfilerClient.js";

export class HiProfilerClient {
    private _client: ProfilerClient;
    private _address: Address;

    get client(): ProfilerClient {
        return this._client;
    }

    set client(value: ProfilerClient) {
        this._client = value;
    }

    get address(): Address {
        return this._address;
    }

    set address(value: Address) {
        this._address = value;
    }

    public constructor(clients: ProfilerClient, addr: Address) {
        this._client = clients;
        this._address = addr;
    };

    public getProfilerClient(): ProfilerClient{
       return this._client;
    }

    public getCapabilities() {
        // this.client.start()
        // this.client.getCapabilities(
    }

    public createSession() {
        // this.client.createSession(
    }

    public startSession() {
        // this.client.startSession(
    }

    public stopSession() {
        // this.client.stopSession(
    }

    public destroySession() {
        // this.client.destroySession(
    }

    public keepSession() {
        // this.client.keepSession(
    }
}