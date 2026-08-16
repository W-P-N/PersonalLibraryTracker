-- V2__rename_token_to_token_hash.sql
ALTER TABLE refresh_tokens RENAME COLUMN token TO token_hash;
