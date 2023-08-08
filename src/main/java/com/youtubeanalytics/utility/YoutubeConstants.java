package com.pacescape.userms.youtube.utility;

public class YoutubeConstants {

    private YoutubeConstants() {
        throw new IllegalStateException("Utility class");
    }

    // Strings used in logger
    public static final String ANALYTICAPI_BASE_URL="https://youtubeanalytics.googleapis.com/v2/reports";
    public static final String DATAAPI_BASE_URL="https://www.googleapis.com/youtube/v3/channels";

    public static final String DATAAPI_PARAMETER_PART="snippet,statistics";

    public static final String ANALYTICAPI_PARAMETER_METRICS="views,redViews,comments,likes,dislikes,videosAddedToPlaylists,videosRemovedFromPlaylists,shares,estimatedMinutesWatched,estimatedRedMinutesWatched,averageViewDuration,averageViewPercentage,annotationClickThroughRate,annotationCloseRate,annotationImpressions,annotationClickableImpressions,annotationClosableImpressions,annotationClicks,annotationCloses,cardClickRate,cardTeaserClickRate,cardImpressions,cardTeaserImpressions,cardClicks,cardTeaserClicks,subscribersGained,subscribersLost";
    public static final String ACCESSTOKEN_REFRESH_URL="https://oauth2.googleapis.com/token";

}
