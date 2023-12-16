package com.example.liwid_app.data.model

import com.google.gson.annotations.SerializedName

data class MatchResponse(
    val success:Int,
    val result:List<MatchData>
)
data class MatchData(
    @SerializedName("event_status")
    val eventStatus: String?,

    @SerializedName("league_name")
    val leagueName: String?,

    @SerializedName("event_key")
    val matchType: Int?,

    @SerializedName("event_home_team")
    val homeTeamName: String?,

    @SerializedName("event_away_team")
    val awayTeamName: String?,

    @SerializedName("home_team_logo")
    val homeTeamLogo: String?,

    @SerializedName("away_team_logo")
    val awayTeamLogo: String?,

    @SerializedName("event_home_final_result")
    val homeTeamResult: String?,

    @SerializedName("event_away_final_result")
    val awayTeamResult: String?
)