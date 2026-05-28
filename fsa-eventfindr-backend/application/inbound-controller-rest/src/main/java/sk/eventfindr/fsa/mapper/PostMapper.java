package sk.eventfindr.fsa.mapper;

import org.mapstruct.Mapper;
import sk.eventfindr.fsa.controller.PostRestController;
import sk.eventfindr.fsa.domain.Post;
import sk.eventfindr.fsa.domain.PostMedia;
import sk.eventfindr.fsa.domain.User;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    default PostRestController.PostDto toDto(Post post) {
        return new PostRestController.PostDto(
                post.getId(),
                post.getContent(),
                post.getCreated(),
                post.getUpdated(),
                post.getMediaCount(),
                toAuthorDto(post.getAuthor()),
                post.getMedia().stream().map(media -> toMediaDto(post.getId(), media)).toList()
        );
    }

    default List<PostRestController.PostDto> toDtoList(Collection<Post> posts) {
        return posts.stream().map(this::toDto).toList();
    }

    default PostRestController.PostAuthorDto toAuthorDto(User user) {
        if (user == null) {
            return null;
        }
        return new PostRestController.PostAuthorDto(
                user.getId(),
                user.getName(),
                user.getOrganizationName(),
                user.getArtistName(),
                user.getRole()
        );
    }

    default PostRestController.PostMediaDto toMediaDto(Long postId, PostMedia media) {
        return new PostRestController.PostMediaDto(
                media.getId(),
                media.getMediaType().name(),
                media.getContentType(),
                media.getSortOrder(),
                "/posts/" + postId + "/media/" + media.getId() + "/file"
        );
    }

    default PostRestController.PostMediaResponseDto toMediaResponse(Long postId, PostMedia media) {
        return new PostRestController.PostMediaResponseDto(
                media.getId(),
                media.getMediaType().name(),
                media.getContentType(),
                media.getSortOrder(),
                "/posts/" + postId + "/media/" + media.getId() + "/file"
        );
    }
}
