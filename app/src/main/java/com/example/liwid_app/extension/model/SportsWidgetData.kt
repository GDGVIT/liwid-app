package com.example.liwid_app.extension.model
import com.google.gson.annotations.SerializedName

data class SportsDataResponse(
    var success:Int,
    var result:List<SportsData>
)
data class SportsData(
    @SerializedName("event_status")
    var eventStatus: String,

    @SerializedName("league_name")
    var leagueName: String,

    @SerializedName("event_key")
    var eventId: Int,

    @SerializedName("event_home_team")
    var homeTeamName: String,

    @SerializedName("event_away_team")
    var awayTeamName: String,

    @SerializedName("home_team_logo")
    var homeTeamLogo: String,

    @SerializedName("away_team_logo")
    var awayTeamLogo: String,

    @SerializedName("event_home_final_result")
    var homeTeamResult: String?,

    @SerializedName("event_away_final_result")
    var awayTeamResult: String?,

    @SerializedName("match_result")
    var matchResult: String?
)