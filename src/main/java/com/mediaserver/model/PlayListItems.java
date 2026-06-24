package com.mediaserver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "playlistsItems")
public class PlayListItems {
      // @Id
      // private String id;
      private String mediaId;
      private String title;
      private String fileType;
      private String url;
      private String duration;
      private String thumbnail;
      private Integer displayOrder;

      // Explicit getter if Lombok not working
      public String getMediaId() {
          return mediaId;
      }
}


