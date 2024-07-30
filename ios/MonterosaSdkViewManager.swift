import React

import MonterosaSDKCore
import MonterosaSDKLauncherKit
import MonterosaSDKIdentifyKit
import MonterosaSDKCommon

enum EventType: String {
    case debug
    case experienceMessage
    case identifyEvent
    case error
    case experienceEvent
}

@objc(MonterosaSdkExperienceViewManager)
class MonterosaSdkExperienceViewManager: RCTViewManager {
    
    override func view() -> (MonterosaSdkExperienceView) {
        return MonterosaSdkExperienceView()
    }
    
    @objc override static func requiresMainQueueSetup() -> Bool {
        return true
    }

    @objc func sendMessageToNode(
        _ node: NSNumber, 
        action: NSString, 
        payload: NSDictionary
    ) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(
                forReactTag: node
            ) as! MonterosaSdkExperienceView
            guard let parameters = payload as? [String: Any?] else {
                assertionFailure("Couldn't convert the payload to a map.")
                return
            }
            component.sendMessage(action: action as String, payload: parameters)
        }
    }

    @objc func sendRequestToNode(
        _ node: NSNumber, 
        action: NSString, 
        payload: NSDictionary,
        timeoutSeconds: NSNumber
    ) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(
                forReactTag: node
            ) as! MonterosaSdkExperienceView
            guard let parameters = payload as? [String: Any?] else {
                assertionFailure("Couldn't convert the payload to a map.")
                return
            }
            component.sendRequest(
                action: action as String,
                payload: parameters,
                timeout: timeoutSeconds.doubleValue
            )
        }
    }
}

class MonterosaSdkExperienceView : UIView {
    
    @objc var onMessageReceived: RCTDirectEventBlock?

    @objc var configuration: NSDictionary = [:] {
        didSet {
            let previousConfig = oldValue.toConfiguration()
            guard let currentConfig = configuration.toConfiguration() else {
                assertionFailure("The configuration received from ReactNative code is incorrect. Missing host, or projectId.")
                return
            }
            
            let core = currentConfig.core()

            if currentConfig.isDifferentExperienceThan(previousConfiguration: previousConfig) {
                debug("Recreating experience")

                // Update delegates in Identify.
                if let previousCore = previousConfig?.core() {
                    Identify.from(core: previousCore).remove(delegate: self)
                }
                Identify.from(core: core).add(delegate: self)

                recreateExperience(configuration: currentConfig, core: core)
            }

            // Always set the token as the iOS SDK avoids sending the same event twice.
            updateToken(currentConfig.token, core: core)
        }
    }
    
    func recreateExperience(configuration: Configuration, core: Core) {
        let experience = Launcher.from(core: core).getExperience(
            experienceConfiguration: ExperienceConfiguration(
                type: configuration.embedType,
                eventId: configuration.eventId,
                autoresizesHeight: configuration.autoresizesHeight,
                hidesHeadersAndFooters: configuration.hidesHeadersAndFooters,
                supportsLoadingState: false,
                parameters: configuration.parameters,
                loadingViewProvider: nil,
                errorViewProvider: nil,
                launchesURLsWithBlankTargetToSafari: configuration.launchesURLsWithBlankTargetToSafari,
                isInspectable: configuration.isInspectable
            )
        )

        // Replace the Experiences
        subviews.forEach {
            $0.removeFromSuperview()
            ($0 as? ExperienceView)?.delegate = nil
        }

        addSubview(experience)
        experience.delegate = self

        // You need to ensure you layout the experience correctly
        // For instance, to display on full screen using autolayout,
        // you'll need something like this:
        experience.translatesAutoresizingMaskIntoConstraints = false
        experience.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: 0).isActive = true
        experience.bottomAnchor.constraint(equalTo: bottomAnchor, constant: 0).isActive = true
        experience.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 0).isActive = true
        experience.trailingAnchor.constraint(equalTo: trailingAnchor, constant: 0).isActive = true
    }
    
    func updateToken(_ token: String?, core: Core) {
        guard let token = token else {
            Identify.from(core: core).logout()
            return
        }
        Identify.from(core: core).credentials = UserCredentials(token: token)
    }

    func sendMessage(action: String, payload: [String: Any?]) {
        guard let experienceView = subviews.first as? ExperienceView else {
            debug("Attempted to send a message when the ExperienceView is not yet created. Sending message with action \(action), payload: \(payload)")
            return
        }

        debug("Sending message with action \(action), payload: \(payload)")
        experienceView.sendMessage(action: action, payload: payload)
    }

    func sendRequest(action: String, payload: [String: Any?], timeout: TimeInterval) {
        guard let experienceView = subviews.first as? ExperienceView else {
            debug("Attempted to send a message when the ExperienceView is not yet created. Sending request with action \(action), payload: \(payload)")
            return
        }

        debug("Sending request with action \(action), payload: \(payload)")
        experienceView.sendRequest(action: action, payload: payload, timeout: timeout) { [weak self] result in
            switch result {
            case .success(let message):
                self?.sendReactNativeMessage(
                    type: EventType.experienceMessage,
                    payload: message.toJavascriptDictionary()
                )

            case .failure(let error):
                self?.sendReactNativeMessage(
                    type: EventType.error, 
                    payload: error.toJavascriptDictionary(message: "Failed to receive a response after \(timeout) seconds to the request sent with action \(action), payload: \(payload)")
                )
            }
        }
    }
    
    func debug(_ message: String) {
        sendReactNativeMessage(type: EventType.debug, payload: ["message": message])
    }

    func sendReactNativeMessage(type: EventType, payload: [String: Any?]) {
        onMessageReceived?([
            "type": type.rawValue,
            "payload": payload
        ])
    }
}

extension MonterosaSdkExperienceView: ExperienceViewDelegate {

    enum ExperienceEventType: String {
        case didStartLoading
        case didEndLoading
        case didFailLoading
        case didChangeIntrinsicSize
        case didBecomeReady
    }
    
    func didStartLoading(experienceView: MonterosaSDKLauncherKit.ExperienceView) {
        sendReactNativeMessage(
            type: EventType.experienceEvent,
            payload: ["event": ExperienceEventType.didStartLoading.rawValue]
        )
    }
    
    func didEndLoading(experienceView: MonterosaSDKLauncherKit.ExperienceView) {
        sendReactNativeMessage(
            type: EventType.experienceEvent,
            payload: ["event": ExperienceEventType.didEndLoading.rawValue]
        )
    }
    
    func didFailLoading(experienceView: MonterosaSDKLauncherKit.ExperienceView, error: Error) {
        sendReactNativeMessage(
            type: EventType.experienceEvent,
            payload: [
                "event": ExperienceEventType.didFailLoading.rawValue,
                "error": error.toJavascriptDictionary(message: "Failed to load")
            ]
        )
    }
    
    func didChangeIntrinsicSize(experienceView: MonterosaSDKLauncherKit.ExperienceView, size: CGSize) {
        sendReactNativeMessage(
            type: EventType.experienceEvent,
            payload: [
                "event": ExperienceEventType.didChangeIntrinsicSize.rawValue,
                "size": [
                    "width": size.width,
                    "height": size.height
                ]
            ]
        )
    }
    
    func didReceiveMessage(experienceView: MonterosaSDKLauncherKit.ExperienceView, message: MonterosaSDKLauncherKit.MessageData) {
        sendReactNativeMessage(
            type: EventType.experienceMessage,
            payload: message.toJavascriptDictionary()
        )
    }
    
    func didBecomeReady(experienceView: MonterosaSDKLauncherKit.ExperienceView) {
        sendReactNativeMessage(
            type: EventType.experienceEvent,
            payload: [ "event": ExperienceEventType.didBecomeReady.rawValue ]
        )
    }
}

extension MonterosaSdkExperienceView: IdentifyKitDelegate {
    enum IdentifyEventType: String {
        case didUpdateCredentials
        case didUpdateUserData
        case didUpdateSessionSignature
        case didRequestLoginByExperience
        case didFailCredentialsValidation
    }
    
    func didUpdateCredentials(credentials: MonterosaSDKIdentifyKit.UserCredentials?) {
        sendReactNativeMessage(
            type: EventType.identifyEvent,
            payload: [
                "event": IdentifyEventType.didUpdateCredentials.rawValue,
                "credentials": credentials?.token
            ]
        )
    }
    
    func didUpdateUserData(userData: MonterosaSDKIdentifyKit.UserData?) {
        sendReactNativeMessage(
            type: EventType.identifyEvent,
            payload: [
                "event": IdentifyEventType.didUpdateUserData.rawValue,
                "userData": userData
            ]
        )
    }

    func didUpdateSessionSignature(signature: Signature?) {
        var signatureDictionary: [String: Any]? = nil

        if let signature = signature {
            signatureDictionary = [
                "sig": signature.signature,
                "timestamp": (signature.timeStamp?.millisecondsSince1970 ?? 0) / 1000,
                "userId": signature.userId
            ]
        }

        sendReactNativeMessage(
            type: EventType.identifyEvent,
            payload: [
                "event": IdentifyEventType.didUpdateSessionSignature.rawValue,
                "signature": signatureDictionary
            ]
        )
    }
    
    func didRequestLoginByExperience() {
        sendReactNativeMessage(
            type: EventType.identifyEvent,
            payload: [
                "event": IdentifyEventType.didRequestLoginByExperience.rawValue
            ]
        )
    }
    
    func didFailCredentialsValidation(error: Error) {
        sendReactNativeMessage(
            type: EventType.identifyEvent,
            payload: [
                "event": IdentifyEventType.didFailCredentialsValidation.rawValue,
                "error": error.toJavascriptDictionary(message: "Failed to validate credentials.")
            ]
        )
    }
}

struct Configuration {
    let host: String
    let projectId: String
    let eventId: String?
    let token: String?
    let experienceUrl: String?
    let embedType: ExperienceType
    let parameters: [String: String]
    let autoresizesHeight: Bool
    let hidesHeadersAndFooters: Bool
    let launchesURLsWithBlankTargetToSafari: Bool
    let isInspectable: Bool

    func isDifferentExperienceThan(previousConfiguration: Configuration?) -> Bool {
        guard let prev = previousConfiguration else {
            return true
        }

        return prev.host != host ||
            prev.projectId != projectId ||
            eventId != prev.eventId ||
            experienceUrl != prev.experienceUrl ||
            parameters != prev.parameters
    }

    func core() -> Core {
        let registeredCore = Core.core(by: coreId())
        
        if let core = registeredCore {
            return core
        }
        
        Core.configure(host: host, projectId: projectId, name: coreId())
        
        let core = Core.core(by: coreId())
        
        if core == nil {
            assertionFailure("We just set the core so shouldn't crash?")
        }
        
        return core!
    }
    
    func coreId() -> String {
        return (host + "-----" + projectId)
    }
}

extension NSDictionary {
    func toConfiguration() -> Configuration? {
        guard let host = self["host"] as? String,
              let projectId = self["projectId"] as? String else {
            return nil
        }

        var embedType = ExperienceType.embed

        if let experienceUrl = self["experienceUrl"] as? String, let url = URL(string: experienceUrl) {
            embedType = .custom(url: url)
        }
        
        return Configuration(
            host: host,
            projectId: projectId,
            eventId: self["eventId"] as? String,
            token: self["token"] as? String,
            experienceUrl: self["experienceUrl"] as? String,
            embedType: embedType,
            parameters: self["parameters"] as? [String: String] ?? [:],
            autoresizesHeight: self["autoresizesHeight"] as? Bool ?? false,
            hidesHeadersAndFooters: self["hidesHeadersAndFooters"] as? Bool ?? true,
            launchesURLsWithBlankTargetToSafari: self["launchesURLsWithBlankTargetToBrowser"] as? Bool ?? true,
            isInspectable: self["isInspectable"] as? Bool ?? false
        )
    }
}

extension MessageData {
    func toJavascriptDictionary() -> [String: Any?] {
        return [
            "action": self.action,
            "payload": self.payload,
            "respondingTo": self.respondingTo as Any,
            "source": self.source
        ]
    }
}

extension Error {
    func toJavascriptDictionary(message: String) -> [String: Any?] {
        return [
            "error": self.localizedDescription,
            "message": message
        ]
    }
}
