package com.kindtail.adoptmate.member.controller;

import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.auth.SecurityUtil;
import com.kindtail.adoptmate.common.dto.CommonResponse;
import com.kindtail.adoptmate.common.dto.SuccessCode;
import com.kindtail.adoptmate.member.dto.*;
import com.kindtail.adoptmate.member.facade.MemberFacade;
import com.kindtail.adoptmate.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adoptmate")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

    private final MemberFacade memberFacade;
    private final MemberService memberService;

    @Override
    @PostMapping("/register")
    public ResponseEntity<CommonResponse<MemberResponse>> registerMember(@RequestBody @Valid MemberRegisterRequest request) {
        MemberResponse response = memberFacade.registerMember(request);
        return CommonResponse.toResponseEntity(SuccessCode.MEMBER_REGISTER_SUCCESS, response);
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<MemberLoginResponse>> login(@RequestBody @Valid MemberLoginRequest dto) {
        MemberLoginResponse result = memberService.login(dto);
        return CommonResponse.toResponseEntity(SuccessCode.LOGIN_SUCCESS, result);
    }

    @Override
    @PostMapping("/refresh-token")
    public ResponseEntity<CommonResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        TokenRefreshResponse response = memberService.refreshAccessToken(request.refreshToken());
        return CommonResponse.toResponseEntity(
                SuccessCode.TOKEN_REISSUE_SUCCESS,
                response
        );
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(HttpServletRequest request) {
        String accessToken = SecurityUtil.resolveToken(request);
        if (accessToken != null) {
            memberService.logout(accessToken);
        }
        return CommonResponse.toResponseEntity(SuccessCode.LOGOUT_SUCCESS);
    }

    @Override
    @GetMapping("/myInfo")
    public ResponseEntity<CommonResponse<MemberInfoResponse>> getMyInfo() {
        MemberInfoResponse dto = memberService.getMemberInfo();
        return CommonResponse.toResponseEntity(SuccessCode.MEMBER_INFO_SUCCESS, dto);
    }

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<List<MemberInfoResponse>>> getAllMembers() {
        List<MemberInfoResponse> dtoList = memberService.getMembers();
        return CommonResponse.toResponseEntity(SuccessCode.MEMBER_ALL_SUCCESS, dtoList);
    }

    @Override
    @PostMapping("/password")
    public ResponseEntity<CommonResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PasswordChangeRequest dto
    ) {
        memberService.changePassword(userDetails.getEmail(), dto);
        return CommonResponse.toResponseEntity(SuccessCode.PASSWORD_CHANGE_SUCCESS);
    }

    @Override
    @DeleteMapping("/delete")
    public ResponseEntity<CommonResponse<Void>> deleteMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        String accessToken = SecurityUtil.resolveToken(request);
        memberService.deleteUser(userDetails.getEmail(), accessToken);
        return CommonResponse.toResponseEntity(SuccessCode.MEMBER_DELETE_SUCCESS);
    }

    @Override
    @DeleteMapping({"/admin/{memberId}", "/admin/member/{memberId}"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Void>> deleteMemberByAdmin(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long adminId = userDetails != null ? userDetails.getId() : SecurityUtil.getCurrentUserId();
        memberService.deleteMemberByAdmin(memberId, adminId);
        return CommonResponse.toResponseEntity(SuccessCode.ADMIN_MEMBER_DELETE_SUCCESS);
    }
}
