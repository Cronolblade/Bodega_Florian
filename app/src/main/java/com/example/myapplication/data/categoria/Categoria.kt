package com.example.myapplication.data.categoria

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class Categoria (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String
)

