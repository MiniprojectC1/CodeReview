package springc1.miniproject.service;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springc1.miniproject.controller.request.PostRequestDto;
import springc1.miniproject.controller.response.CommentResponseDto;
import springc1.miniproject.controller.response.PostAllResponseDto;
import springc1.miniproject.controller.response.PostResponseDto;
import springc1.miniproject.controller.response.ResponseDto;
import springc1.miniproject.domain.Comment;
import springc1.miniproject.domain.Member;
import springc1.miniproject.domain.Post;
import springc1.miniproject.domain.UserDetailsImpl;
import springc1.miniproject.exception.ErrorMessage;
import springc1.miniproject.exception.post.PostException;
import springc1.miniproject.repository.CommentRepository;
import springc1.miniproject.repository.PostRepository;


@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public ResponseDto<?> createPost(PostRequestDto requestDto, UserDetailsImpl userDetails ){
        Member member = userDetails.getMember();

        Post post = new Post(requestDto, member);

        postRepository.save(post);

        return ResponseDto.success( new PostResponseDto(post,null));
    }

    @Transactional(readOnly = true)
    public ResponseDto<?> getPost(Long id) {
        Post post = getPostById(id);
        return ResponseDto.success( createPostResponseDto(post));
    }

    @Transactional(readOnly = true)
    public ResponseDto<?> getAllPost() {
        List<Post> postList = postRepository.findAllByOrderByModifiedAtDesc();

        List<PostAllResponseDto> posts = getAllPosts(postList);

        return ResponseDto.success(posts);
    }

    private List<PostAllResponseDto> getAllPosts(List<Post> postList) {
        List<PostAllResponseDto> posts = new ArrayList<>();

        postList.forEach(post -> {
            List<Comment> commentList = commentRepository.findAllByPost(post);
            posts.add(new PostAllResponseDto(post, commentList));
        });

        return posts;
    }

    @Transactional
    public ResponseDto<?> updatePost(Long id, PostRequestDto requestDto, UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        Post post = getPostById(id);

        memberValidatePost(member, post);

        post.update(requestDto);

        return ResponseDto.success( createPostResponseDto(post));
    }

    private PostResponseDto createPostResponseDto(Post post) {
        List<CommentResponseDto> comments = getAllCommentsByPost(post);
        return new PostResponseDto(post, comments);
    }

    private List<CommentResponseDto> getAllCommentsByPost(Post post) {
        return commentRepository.findAllByPost(post).stream()
            .map(comment -> new CommentResponseDto(comment))
            .collect(toList());
    }

    @Transactional
    public ResponseDto<?> deletePost(Long id, UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        Post post = getPostById(id);

        memberValidatePost(member, post);

        postRepository.delete(post);
        return ResponseDto.success("delete success");

    }

    private void memberValidatePost(Member member, Post post) {
        if (!post.getMember().equals(member)) {
            throw new PostException(ErrorMessage.ERROR_MESSAGE);
        }
    }

    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글 id 입니다"));
    }
}