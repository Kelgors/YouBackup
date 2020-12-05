## uBackup-S3

### config.yml (in plugins/uBackup)

```yaml
backups:
  your_profile:
    enabled: true
    cron: 0 3 * * SUN # every sunday at 3:00
    filename: "{date}{time}.zip"
    rotation: 5
    compression:
      type: zip
      include:
        - plugins/
      # ...
    destination:
      # Set it to s3
      type: s3
      # this is your S3 bucket name 
      bucket: your_bucket
      # this is the path inside your bucket where 
      # you want your backups
      path: backups/your_profile
      # your bucket region
      region: eu-west-3
      # Authentication information (unsecure)
      client_id: insert_your_client_id
      client_secret: insert_your_secret
      # Authentication information (secure) (https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-profiles.html)
      # if profile is set, client_id & client_secret are ignored
      profile: mc-backup
```
