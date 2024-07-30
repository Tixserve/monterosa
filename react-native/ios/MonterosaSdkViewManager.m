#import <React/RCTViewManager.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(MonterosaSdkExperienceViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(configuration, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(onMessageReceived, RCTDirectEventBlock)

RCT_EXTERN_METHOD(
  sendMessageToNode:(nonnull NSNumber *)node
  action:(NSString *) action
  payload:(NSDictionary *) payload
)

RCT_EXTERN_METHOD(
  sendRequestToNode:(nonnull NSNumber *)node
  action:(NSString *) action
  payload:(NSDictionary *) payload
  timeoutSeconds: (NSNumber*) timeoutSeconds
)

@end
