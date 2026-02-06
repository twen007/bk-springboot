BEGIN
    DBMS_SCHEDULER.create_job (
        job_name        => 'notify_bao_job',  -- Name of the job
        job_type        => 'PLSQL_BLOCK',       -- Type of job
        job_action      => 'BEGIN notify_ao_cost_over_approved; END;',  -- Action to perform
        start_date      => SYSTIMESTAMP + INTERVAL '1' DAY,  -- Start date (next occurrence)
        repeat_interval  => 'FREQ=WEEKLY; BYDAY=MON; BYHOUR=6; BYMINUTE=0; BYSECOND=0',  -- Schedule
        enabled         => TRUE,                -- Enable the job
        comments        => 'Job to run notify_user every Monday at 6 AM'  -- Job description
    );
END;
/