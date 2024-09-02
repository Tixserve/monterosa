import {
  requireNativeComponent,
  UIManager,
  Platform,
  type ViewStyle,
  findNodeHandle,
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-monterosa-sdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

type MonterosaSdkProps = {
  style: ViewStyle;
  configuration: {
    host: string;
    experienceUrl?: string;
    projectId: string;
    eventId?: string;
    token?: string;
    parameters?: { [key: string]: string };
    autoresizesHeight?: boolean;
    hidesHeadersAndFooters?: boolean;
    launchesURLsWithBlankTargetToBrowser?: boolean;
    isInspectable?: boolean;
  };
  onMessageReceived: any;
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
