import { type ViewStyle, type NativeSyntheticEvent } from 'react-native';
export type MonterosaSdkError = {
    message: string;
    error: string;
};
export type MonterosaExperienceEventPayload = {
    event: 'didStartLoading';
} | {
    event: 'didEndLoading';
} | {
    event: 'didBecomeReady';
} | {
    event: 'didFailLoading';
    error: MonterosaSdkError;
} | {
    event: 'didChangeIntrinsicSize';
    size: {
        width: number;
        height: number;
    };
} | {
    event: 'didRequestShare';
    share: {
        url: string;
        title: string;
        description: string;
        imageUrl: string;
    };
} | {
    event: 'onDisplayedFullScreenChanged';
    fullscreen: boolean;
};
export type MonterosaIdentifyEventPayload = {
    event: 'didUpdateCredentials';
    credentials: string | null;
} | {
    event: 'didUpdateUserData';
    userData: Record<string, unknown> | null;
} | {
    event: 'didUpdateSessionSignature';
    signature: {
        sig: string;
        timestamp: number;
        userId: string;
    } | null;
} | {
    event: 'didRequestLoginByExperience';
} | {
    event: 'didFailCredentialsValidation';
    error: MonterosaSdkError;
};
export type MonterosaExperienceMessagePayload = {
    action: string;
    payload: Record<string, unknown>;
    respondingTo: string | null;
    source: string;
};
export type MonterosaDebugPayload = {
    message: string;
};
export type MonterosaSdkNativeEvent = {
    type: 'experienceEvent';
    payload: MonterosaExperienceEventPayload;
} | {
    type: 'identifyEvent';
    payload: MonterosaIdentifyEventPayload;
} | {
    type: 'experienceMessage';
    payload: MonterosaExperienceMessagePayload;
} | {
    type: 'debug';
    payload: MonterosaDebugPayload;
} | {
    type: 'error';
    payload: MonterosaSdkError;
};
export type MonterosaMessageEvent = NativeSyntheticEvent<MonterosaSdkNativeEvent>;
export type MonterosaSdkConfiguration = {
    host: string;
    projectId: string;
    experienceUrl?: string;
    eventId?: string;
    token?: string;
    backgroundColor?: string;
    parameters?: {
        [key: string]: string;
    };
    autoresizesHeight?: boolean;
    hidesHeadersAndFooters?: boolean;
    allowsPopupBehavior?: boolean;
    showsDefaultShareSheet?: boolean;
    launchesURLsWithBlankTargetToBrowser?: boolean;
    isInspectable?: boolean;
    allowsInlineMediaPlayback?: boolean;
};
type MonterosaSdkProps = {
    style: ViewStyle;
    configuration: MonterosaSdkConfiguration;
    onMessageReceived: (event: MonterosaMessageEvent) => void;
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