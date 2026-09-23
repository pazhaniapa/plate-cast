import FirebaseCore
import GoogleSignIn
import Shared
import UIKit

final class GoogleSignInHandlerImpl: NSObject, GoogleSignInHandler {

    func requestIdToken(onResult: @escaping (String?, String?) -> Void) {
        guard let clientID = FirebaseApp.app()?.options.clientID else {
            onResult(nil, "Firebase is not configured")
            return
        }
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)

        guard let presentingViewController = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .flatMap({ $0.windows })
            .first(where: { $0.isKeyWindow })?.rootViewController
        else {
            onResult(nil, "No presenting view controller")
            return
        }

        GIDSignIn.sharedInstance.signIn(withPresenting: presentingViewController) { result, error in
            if let error = error as NSError?, error.code == GIDSignInError.canceled.rawValue {
                onResult(nil, nil)
                return
            }
            if let error = error {
                onResult(nil, error.localizedDescription)
                return
            }
            guard let idToken = result?.user.idToken?.tokenString else {
                onResult(nil, "No ID token returned")
                return
            }
            onResult(idToken, nil)
        }
    }
}
