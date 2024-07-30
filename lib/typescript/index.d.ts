import { type ViewStyle } from 'react-native';
type MonterosaSdkProps = {
    style: ViewStyle;
    configuration: {
        host: string;
        experienceUrl: string;
        projectId: string;
        eventId?: string;
        token?: string;
        parameters?: {
            [key: string]: string;
        };
        autoresizesHeight?: boolean;
        hidesHeadersAndFooters?: boolean;
        launchesURLsWithBlankTargetToBrowser?: boolean;
        isInspectable?: boolean;
    };
    onMessageReceived: any;
    ref: any;
};
export declare const MonterosaSdkExperienceView: import("react-native").HostComponent<MonterosaSdkProps> | (() => never);
type Payload = {
    [key: string]: any;
};
export declare const sendMessage: (ref: React.MutableRefObject<number>, action: string, payload: Payload) => void;
export declare const sendRequest: (ref: React.MutableRefObject<number>, action: string, payload: Payload, timeoutSeconds: number | null) => void;
export {};
//# sourceMappingURL=index.d.ts.map