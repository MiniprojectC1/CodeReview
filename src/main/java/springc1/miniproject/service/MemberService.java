package springc1.miniproject.service;

import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springc1.miniproject.controller.request.LoginRequestDto;
import springc1.miniproject.controller.request.MemberRequestDto;
import springc1.miniproject.controller.request.TokenDto;
import springc1.miniproject.controller.response.MemberResponseDto;
import springc1.miniproject.controller.response.ResponseDto;
import springc1.miniproject.domain.Member;
import springc1.miniproject.jwt.TokenProvider;
import springc1.miniproject.repository.MemberRepository;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional
    public ResponseDto<?> createMember(MemberRequestDto requestDto) {

        isPresentMember(requestDto.getUsername());

        String password = passwordEncoder.encode(requestDto.getPassword());

        Member member = new Member(requestDto, password);

        memberRepository.save(member);
        return ResponseDto.success( new MemberResponseDto(member));

    }

    @Transactional(readOnly = true)
    public Member isPresentMember(String username) {
        return memberRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("중복된 닉네임 입니다."));
    }

    @Transactional
    public ResponseDto<?> login(LoginRequestDto requestDto, HttpServletResponse response) {
        Member member = getByUsername(requestDto.getUsername());

        passwordCheck(member.getPassword(), requestDto.getPassword());

        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        tokenToHeaders(tokenDto, response);

        return ResponseDto.success(new MemberResponseDto(member));

    }

    private void passwordCheck(String password, String comfirmPassword) {
        if (passwordEncoder.matches(password, comfirmPassword)){
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
         }
    }

    private void tokenToHeaders(TokenDto tokenDto, HttpServletResponse response) {
        response.addHeader("AccessToken", "Bearer " + tokenDto.getAccessToken());
    }


    public Member getByUsername(String username) {
        return memberRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("ggggg"));
    }
}
