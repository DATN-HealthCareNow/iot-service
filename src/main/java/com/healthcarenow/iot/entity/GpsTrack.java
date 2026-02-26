package com.healthcarenow.iot.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Document(collection = "gps_tracks")
public class GpsTrack {

  @Id
  private String id;

  @Indexed
  private String activityId;

  // Optional: Only string representation. If you need GeoSpatial Index, it should
  // be on a Point field.
  // However, schema specifies points as a List.
  // To index a list of points properly in MongoDB, the field within the list item
  // should be indexed.
  // For simplicity: We will add @GeoSpatialIndexed on the appropriate loc
  // property in TrackPoint or on the collection level via Mongock/MongoTemplate
  // if needed.
  private List<TrackPoint> points;

  // TTL index for 30 days = 30 * 24 * 60 * 60 = 2592000 seconds
  @Indexed(expireAfterSeconds = 2592000)
  private Instant expiresAt;

  @Data
  @Builder
  public static class TrackPoint {
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint loc;
    private Instant ts;
    private double acc;
  }
}
