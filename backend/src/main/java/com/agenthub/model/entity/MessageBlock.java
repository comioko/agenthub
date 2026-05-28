package com.agenthub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_block")
public class MessageBlock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long messageId;

    private String blockType; // code/diff/web/file/deploy

    private String content;

    private String language;

    private String title;

    private String metadata; // JSON string

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
