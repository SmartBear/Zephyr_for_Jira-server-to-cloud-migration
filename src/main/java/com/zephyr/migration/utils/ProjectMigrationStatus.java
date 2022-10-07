package com.zephyr.migration.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectMigrationStatus {
    public ProgressStatusLevel status = ProgressStatusLevel.READY;
    //public Map<String, EntityCounts> counts = new HashMap<>();
    public List<String> statusSteps = new ArrayList<>();

}


