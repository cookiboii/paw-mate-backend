package com.kindtail.adoptmate.auth;

/**
 * 소셜 로그인(OAuth2 / Kakao) 성공 시 팝업 창 postMessage HTML 응답을 생성하는 공통 유틸리티
 */
public abstract class OAuthResponseUtil {

    private OAuthResponseUtil() {
    }

    public static String buildPopupSuccessHtml(String token, String refreshToken, Long memberId, String role, String provider, String targetOrigin) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head><title>소셜 로그인 완료</title></head>
                <body>
                    <script>
                        if (window.opener) {
                            window.opener.postMessage({
                                type: 'OAUTH_SUCCESS',
                                token: '%s',
                                refreshToken: '%s',
                                id: '%s',
                                role: '%s',
                                provider: '%s'
                            }, '%s');
                            window.close();
                        } else {
                            window.location.href = '%s/';
                        }
                    </script>
                    <p>소셜 로그인 성공! 처리 중...</p>
                </body>
                </html>
                """,
                token,
                refreshToken,
                memberId != null ? memberId.toString() : "",
                role != null ? role : "",
                provider != null ? provider : "",
                targetOrigin,
                targetOrigin
        );
    }
}
