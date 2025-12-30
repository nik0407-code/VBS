package com.vbs.demo.repositories;

import com.vbs.demo.models.Updates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface UpdatesRepo extends JpaRepository<Updates,Integer> {
}
