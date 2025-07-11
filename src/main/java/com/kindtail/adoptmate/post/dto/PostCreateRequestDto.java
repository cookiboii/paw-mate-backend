package com.kindtail.adoptmate.post.dto;

import java.time.LocalDateTime;

public record PostCreateRequestDto(String name , String content , String img , String title , LocalDateTime  dateTime) {


}
