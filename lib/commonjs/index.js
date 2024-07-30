"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.sendRequest = exports.sendMessage = exports.MonterosaSdkExperienceView = void 0;
var _reactNative = require("react-native");
const LINKING_ERROR = `The package 'react-native-monterosa-sdk' doesn't seem to be linked. Make sure: \n\n` + _reactNative.Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n' + '- You are not using Expo Go\n';
const ComponentName = 'MonterosaSdkExperienceView';
const MonterosaSdkExperienceView = _reactNative.UIManager.getViewManagerConfig(ComponentName) != null ? (0, _reactNative.requireNativeComponent)(ComponentName) : () => {
  throw new Error(LINKING_ERROR);
};
exports.MonterosaSdkExperienceView = MonterosaSdkExperienceView;
const sendMessage = (ref, action, payload) => {
  var _viewManagerConfig$Co;
  const viewManagerConfig = _reactNative.UIManager.getViewManagerConfig(ComponentName);
  const sendMessageToNodeCommand = viewManagerConfig === null || viewManagerConfig === void 0 || (_viewManagerConfig$Co = viewManagerConfig.Commands) === null || _viewManagerConfig$Co === void 0 ? void 0 : _viewManagerConfig$Co.sendMessageToNode;
  if (sendMessageToNodeCommand == null) {
    console.error('Failed to retrieve sendMessage command.');
    return;
  }
  const command = sendMessageToNodeCommand;
  _reactNative.UIManager.dispatchViewManagerCommand((0, _reactNative.findNodeHandle)(ref.current), command, [action, payload]);
};
exports.sendMessage = sendMessage;
const sendRequest = (ref, action, payload, timeoutSeconds) => {
  var _viewManagerConfig$Co2;
  if (timeoutSeconds === null) timeoutSeconds = 10;
  const viewManagerConfig = _reactNative.UIManager.getViewManagerConfig(ComponentName);
  const sendRequestToNodeCommand = viewManagerConfig === null || viewManagerConfig === void 0 || (_viewManagerConfig$Co2 = viewManagerConfig.Commands) === null || _viewManagerConfig$Co2 === void 0 ? void 0 : _viewManagerConfig$Co2.sendRequestToNode;
  if (sendRequestToNodeCommand == null) {
    console.error('Failed to retrieve sendRequest command.');
    return;
  }
  const command = sendRequestToNodeCommand;
  _reactNative.UIManager.dispatchViewManagerCommand((0, _reactNative.findNodeHandle)(ref.current), command, [action, payload, timeoutSeconds]);
};
exports.sendRequest = sendRequest;
//# sourceMappingURL=index.js.map