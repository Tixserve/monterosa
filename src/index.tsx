import {
  requireNativeComponent,
  UIManager,
  Platform,
  type ViewStyle,
  type NativeSyntheticEvent,
  findNodeHandle,
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-monterosa-sdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// ---------------------------------------------------------------------------
// Event payload types
// ---------------------------------------------------------------------------

export type MonterosaSdkError = {
  message: string;
  error: string;
};

export type MonterosaExperienceEventPayload =
  | { event: 'didStartLoading' }
  | { event: 'didEndLoading' }
  | { event: 'didBecomeReady' }
  | { event: 'didFailLoading'; error: MonterosaSdkError }
  | { event: 'didChangeIntrinsicSize'; size: { width: number; height: number } }
  | {
      event: 'didRequestShare';
      share: {
        url: string;
        title: string;
        description: string;
        imageUrl: string;
      };
    }
  | { event: 'onDisplayedFullScreenChanged'; fullscreen: boolean }; // Android only

export type MonterosaIdentifyEventPayload =
  | { event: 'didUpdateCredentials'; credentials: string | null }
  | { event: 'didUpdateUserData'; userData: Record<string, unknown> | null }
  | {
      event: 'didUpdateSessionSignature';
      signature: { sig: string; timestamp: number; userId: string } | null;
    }
  | { event: 'didRequestLoginByExperience' }
  | { event: 'didFailCredentialsValidation'; error: MonterosaSdkError };

export type MonterosaExperienceMessagePayload = {
  action: string;
  payload: Record<string, unknown>;
  respondingTo: string | null;
  source: string;
};

export type MonterosaDebugPayload = { message: string };

// ---------------------------------------------------------------------------
// Top-level event — discriminate on `nativeEvent.type`
// Note: `e.type` mirrors `nativeEvent.type` at runtime but is not narrowed
// by TypeScript. Use `e.nativeEvent.type` for type-safe narrowing.
// ---------------------------------------------------------------------------

export type MonterosaSdkNativeEvent =
  | { type: 'experienceEvent'; payload: MonterosaExperienceEventPayload }
  | { type: 'identifyEvent'; payload: MonterosaIdentifyEventPayload }
  | { type: 'experienceMessage'; payload: MonterosaExperienceMessagePayload }
  | { type: 'debug'; payload: MonterosaDebugPayload }
  | { type: 'error'; payload: MonterosaSdkError };

export type MonterosaMessageEvent =
  NativeSyntheticEvent<MonterosaSdkNativeEvent>;

// ---------------------------------------------------------------------------
// SDK configuration
// ---------------------------------------------------------------------------

export type MonterosaSdkConfiguration = {
  host: string;
  projectId: string;
  experienceUrl?: string;
  eventId?: string;
  token?: string;
  backgroundColor?: string;
  parameters?: { [key: string]: string };
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

const ComponentName = 'MonterosaSdkExperienceView';

export const MonterosaSdkExperienceView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<MonterosaSdkProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

type Payload = { [key: string]: any };

export const sendMessage = (
  ref: React.MutableRefObject<number>,
  action: string,
  payload: Payload
) => {
  const viewManagerConfig = UIManager.getViewManagerConfig(ComponentName);
  const sendMessageToNodeCommand =
    viewManagerConfig?.Commands?.sendMessageToNode;

  if (sendMessageToNodeCommand == null) {
    console.error('Failed to retrieve sendMessage command.');
    return;
  }

  const command: number = sendMessageToNodeCommand;
  UIManager.dispatchViewManagerCommand(findNodeHandle(ref.current), command, [
    action,
    payload,
  ]);
};

export const sendRequest = (
  ref: React.MutableRefObject<number>,
  action: string,
  payload: Payload,
  timeoutSeconds: number | null
) => {
  if (timeoutSeconds === null) timeoutSeconds = 10;

  const viewManagerConfig = UIManager.getViewManagerConfig(ComponentName);
  const sendRequestToNodeCommand =
    viewManagerConfig?.Commands?.sendRequestToNode;

  if (sendRequestToNodeCommand == null) {
    console.error('Failed to retrieve sendRequest command.');
    return;
  }

  const command: number = sendRequestToNodeCommand;

  UIManager.dispatchViewManagerCommand(findNodeHandle(ref.current), command, [
    action,
    payload,
    timeoutSeconds,
  ]);
};
