package com.humboshot.koillection.destinations

interface LoginDestination {
    val route: String
    val title: String
}

object Login : LoginDestination {
    override val route: String = "login"
    override val title: String = "Login"
}