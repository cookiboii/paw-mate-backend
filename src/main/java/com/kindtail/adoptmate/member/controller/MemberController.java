package com.kindtail.adoptmate.member.controller;

import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.auth.SecurityUtil;
import com.kindtail.adoptmate.common.dto.CommonResDto;
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
    public ResponseEntity<CommonResDto<MemberResponseDto>> registerMember(@RequestBody @Valid MemberRegisterRequestDto requestDto) {
        MemberResponseDto responseDto = memberFacade.registerMember(requestDto);
        return CommonResDto.toResponseEntity(SuccessCode.MEMBER_REGISTER_SUCCESS, responseDto);
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<CommonResDto<MemberLoginResultDto>> login(@RequestBody @Valid MemberLoginRequestDto dto) {
        MemberLoginResultDto result = memberService.login(dto);
        return CommonResDto.toResponseEntity(SuccessCode.LOGIN_SUCCESS, result);
    }

    @Override
    @PostMapping("/refresh-token")
    public ResponseEntity<CommonResDto<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        String newToken = memberService.refreshAccessToken(request.refreshToken());
        return CommonResDto.toResponseEntity(
                SuccessCode.TOKEN_REISSUE_SUCCESS,
                new TokenRefreshResponse(newToken)
        );
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<CommonResDto<Void>> logout(HttpServletRequest request) {
        String accessToken = SecurityUtil.resolveToken(request);
        if (accessToken != null) {
            memberService.logout(accessToken);
        }
        return CommonResDto.toResponseEntity(SuccessCode.LOGOUT_SUCCESS);
    }

    @Override
    @GetMapping("/myInfo")
    public ResponseEntity<CommonResDto<MemberInfoResponseDto>> getMyInfo() {
        MemberInfoResponseDto dto = memberService.getMemberInfo();
        return CommonResDto.toResponseEntity(SuccessCode.MEMBER_INFO_SUCCESS, dto);
    }

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto<List<MemberInfoResponseDto>>> getAllMembers() {
        List<MemberInfoResponseDto> dtoList = memberService.getMembers();
        return CommonResDto.toResponseEntity(SuccessCode.MEMBER_ALL_SUCCESS, dtoList);
    }

    @Override
    @PostMapping("/password")
    public ResponseEntity<CommonResDto<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PasswordChangeRequestDto dto
    ) {
        memberService.changePassword(userDetails.getEmail(), dto);
        return CommonResDto.toResponseEntity(SuccessCode.PASSWORD_CHANGE_SUCCESS);
    }

    @Override
    @DeleteMapping("/delete")
    public ResponseEntity<CommonResDto<Void>> deleteMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        String accessToken = SecurityUtil.resolveToken(request);
        memberService.deleteUser(userDetails.getEmail(), accessToken);
        return CommonResDto.toResponseEntity(SuccessCode.MEMBER_DELETE_SUCCESS);
    }

    @Override
    @DeleteMapping({"/admin/{memberId}", "/admin/member/{memberId}"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto<Void>> deleteMemberByAdmin(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long adminId = userDetails != null ? userDetails.getId() : SecurityUtil.getCurrentUserId();
        memberService.deleteMemberByAdmin(memberId, adminId);
        return CommonResDto.toResponseEntity(SuccessCode.ADMIN_MEMBER_DELETE_SUCCESS);
    }
}
