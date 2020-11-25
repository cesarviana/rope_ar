package model

import androidx.room.Entity

@Entity
data class User (val name: String) : model.Entity()