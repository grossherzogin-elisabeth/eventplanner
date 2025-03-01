ALTER TABLE positions
    ADD column imo_list_name TEXT NOT NULL default imoListRank;
ALTER TABLE positions
    DROP column imoListRank;
