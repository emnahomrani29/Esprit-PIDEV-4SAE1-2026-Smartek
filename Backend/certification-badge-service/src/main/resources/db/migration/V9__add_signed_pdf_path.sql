-- Store the path/filename of the signed PDF for each earned certification
ALTER TABLE earned_certification
    ADD COLUMN signed_pdf_path VARCHAR(500) NULL;
