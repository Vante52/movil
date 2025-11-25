package com.example.fitmatch.config

import com.google.firebase.database.FirebaseDatabase

object FirebaseDatabaseConfig {
    @Volatile
    private var initialized = false
    fun initialize(){
        if(!initialized){
            synchronized(this){
                if(!initialized){
                    FirebaseDatabase.getInstance().setPersistenceEnabled(true)
                    initialized=true
                }
            }
        }
    }

}