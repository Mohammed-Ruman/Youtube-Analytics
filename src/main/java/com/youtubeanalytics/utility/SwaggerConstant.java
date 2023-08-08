package com.pacescape.userms.youtube.utility;

public class SwaggerConstant {
    public static final String GETAUTHORIZATIONCODESUCCESS = """
            {
            "timestamp": "26-01-2023 07:10:06",
            "status": "Data fetched Successfully",
            "data": null
            }
            """;

    public static final String GETAUTHORIZATIONCODEFAILURE = """
            {
              "timestamp": "26-01-2023 07:16:44",
              "status": 400,
              "message": "Server might  Busy ",
              "debugMessage": null,
              "subErrors": null
            }
            """;

    public static final String GETAUTHENTICATIONCODESUCCESS = """
            {
            "timestamp": "26-01-2023 07:10:06",
            "status": "Data fetched Successfully",
            "data": null
            }
            """;

    public static final String GETAUTHENTICATIONCODEFAILURE = """
            {
              "timestamp": "26-01-2023 07:16:44",
              "status": 400,
               "error": "BAD_REQUEST",
              "message": "Authorization code may already used or Enter a valid Authorization code",
              "debugMessage": null,
              "subErrors": null
            }
            """;

    public static final String FETCHBASICINFORMATIONSUCCESS = """
            {
            "timestamp": "26-01-2023 07:10:06",
            "status": "Data fetched Successfully",
            "data": null
            }
            """;

    public static final String FETCHBASICINFORMATIONFAILURE = """
            {
              "timestamp": "26-01-2023 07:16:44",
              "status": 400,
              "error": "BAD_REQUEST",
              "message": "Invalid Authentication token / mismatch in fields",
              "fields": "These Are all the fields need to be pass id,username,account_type,media_count"
              "debugMessage": null,
              "subErrors": null
            }
            """;
    public static final String GETREFRESHTOKENSUCCESS = """
            {
            "timestamp": "26-01-2023 07:10:06",
            "status": "refresh token fetched Successfully",
            "data": null
            }
            """;

    public static final String GETREFRESHTOKENFAILURE = """
            {
              "timestamp": "26-01-2023 07:16:44",
              "status": 400,
              "error": "BAD_REQUEST",
              "message": "Invalid Access token",
              "debugMessage": null,
              "subErrors": null
            }
            """;

    public static final String FETCHMEDIADETAILSSUCCESS = """
            {
            "timestamp": "26-01-2023 07:10:06",
            "status": "Media Details fetched Successfully",
            "data": null
            }
            """;

    public static final String FETCHMEDIADETAILSFAILURE = """
            {
              "timestamp": "26-01-2023 07:16:44",
              "status": 400,
              "error": "BAD_REQUEST",
              "message": "Invalid MediaId",
              "debugMessage": null,
              "subErrors": null
            }
            """;

    public static final String FETCHCHILDMEDIADETAILSSUCCESS = """
            {
            "timestamp": "26-01-2023 07:10:06",
            "status": " Child Media Details fetched Successfully",
            "data": null
            }
            """;

    public static final String FETCHCHILDMEDIADETAILSFAILURE = """
            {
              "timestamp": "26-01-2023 07:16:44",
              "status": 400,
              "error": "BAD_REQUEST",
              "message": "Invalid Child MediaId",
              "debugMessage": null,
              "subErrors": null
            }
            """;

    public static final String GETYOUTUBEACCOUNTLINKSTATUS_LINKED = "true";

    public static final String GETYOUTUBEACCOUNTLINKSTATUS_NOTLINKED = "false";

}
