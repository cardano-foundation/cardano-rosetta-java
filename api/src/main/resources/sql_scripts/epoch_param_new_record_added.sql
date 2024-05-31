CREATE OR REPLACE FUNCTION notify_epoch_param_new_record() RETURNS TRIGGER AS $$
BEGIN
    PERFORM pg_notify('epoch_param_new_record', NEW.id::text);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER epoch_param_new_record_trigger
    AFTER INSERT ON preprod.epoch_param
    FOR EACH ROW
EXECUTE FUNCTION notify_epoch_param_new_record();