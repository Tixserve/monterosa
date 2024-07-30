import { requireNativeComponent, UIManager, Platform, findNodeHandle } from 'react-native';
const LINKING_ERROR = `The package 'react-native-monterosa-sdk' doesn't seem to be linked. Make sure: \n\n` + Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n' + '- You are not using Expo Go\n';
const ComponentName = 'MonterosaSdkExperienceView';
export const MonterosaSdkExperienceView = UIManager.getViewManagerConfig(ComponentName) != null ? requireNativeComponent(ComponentName) : () => {
  throw new Error(LINKING_ERROR);
};
export const sendMessage = (ref, action, payload) => {
  var _viewManagerConfig$Co;
  const viewManagerConfig = UIManager.getViewManagerConfig(ComponentName);
  const sendMessageToNodeCommand = viewManagerConfig === null || viewManagerConfig === void 0 || (_viewManagerConfig$Co = viewManagerConfig.Commands) === null || _viewManagerConfig$Co === void 0 ? void 0 : _viewManagerConfig$Co.sendMessageToNode;
  if (sendMessageToNodeCommand == null) {
    console.error('Failed to retrieve sendMessage command.');
    return;
  }
  const command = sendMessageToNodeCommand;
  UIManager.dispatchViewManagerCommand(findNodeHandle(ref.current), command, [action, payload]);
};
export const sendRequest = (ref, action, payload, timeoutSeconds) => {
  var _viewManagerConfig$Co2;
  if (timeoutSeconds === null) timeoutSeconds = 10;
  const viewManagerConfig = UIManager.getViewManagerConfig(ComponentName);
  const sendRequestToNodeCommand = viewManagerConfig === null || viewManagerConfig === void 0 || (_viewManagerConfig$Co2 = viewManagerConfig.Commands) === null || _viewManagerConfig$Co2 === void 0 ? void 0 : _viewManagerConfig$Co2.sendRequestToNode;
  if (sendRequestToNodeCommand == null) {
    console.error('Failed to retrieve sendRequest command.');
    return;
  }
  const command = sendRequestToNodeCommand;
  UIManager.dispatchViewManagerCommand(findNodeHandle(ref.current), command, [action, payload, timeoutSeconds]);
};
//# sourceMappingURL=index.js.map