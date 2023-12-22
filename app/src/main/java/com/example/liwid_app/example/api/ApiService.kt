package com.example.liwid_app.example.api

import com.example.liwid_app.example.model.MatchResponse
import com.example.liwid_app.example.util.API_KEY
import com.example.liwid_app.example.util.GAME_TYPE
import com.example.liwid_app.example.util.MATCH_TYPE
import com.example.liwid_app.example.util.league_id
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET(GAME_TYPE)
    fun getMatch(
        @Query("met") eventType:String= MATCH_TYPE,
        @Query("APIkey")apikey:String= API_KEY,
        @Query("leagueID")leagueId:Int= league_id
    ): Call<MatchResponse>
}