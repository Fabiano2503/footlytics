package com.fabianoanticona.footlytics.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteTeamDao {

    @Query("SELECT * FROM favorite_teams ORDER BY name ASC")
    List<FavoriteTeamEntity> getAll();

    @Query("SELECT EXISTS (SELECT 1 FROM favorite_teams WHERE id = :teamId)")
    boolean isFavorite(int teamId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteTeamEntity team);

    @Query("DELETE FROM favorite_teams WHERE id = :teamId")
    void deleteById(int teamId);
}
