package edu.agh.cs.distributedsystems.weatherapi.integrations.meteosource.model

class MeteosourceResponse(
    val daily: DailyResponse,
)

class DailyResponse(
    val data: List<DailyWeather>,
)
