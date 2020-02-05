// Type definitions for CCoap.

interface CCoapOption {
    name: string;
    value: string | number | Array;
}

interface CCoapRequest {
    method?: string;
    uri: string;
    payload?: string | Object | Array;
    options?: CCoapOption[];
    confirmable?: boolean;
}

interface CCoapResponse {
    code: number;
    payload: string | Array;
    options: CCoapOption[];
}

interface CCoapDiscoveredDevice {
    address: string;
    port: number;
    resources: string;
}

interface CCoapInterface {
    get(uri: string): Promise<CCoapResponse>;
    post(uri: string, payload: string | Uint8Array): Promise<CCoapResponse>;
    put(uri: string, payload: string | Uint8Array): Promise<CCoapResponse>;
    delete(uri: string): Promise<CCoapResponse>;
    discover(timeout?: number): Promise<CCoapDiscoveredDevice[]>;
    request(req: CCoapRequest): Promise<CCoapResponse>;
}

declare var CCoap: CCoapInterface;