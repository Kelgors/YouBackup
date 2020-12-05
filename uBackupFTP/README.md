## uBackup-FTP

### config.yml (in plugins/uBackup)

```yaml
backups:
  your_profile:
    enabled: true
    cron: 0 3 * * * # every day at 3:00
    filename: "{date}{time}.zip"
    rotation: 5
    compression:
      type: zip
      worlds:
        - world
        - world_nether
        - world_the_end
      # ...
    destination:
      # Set it to ftp (even if you want to use sftp)
      type: ftp
      # if true, use sftp protocol, otherwise simple ftp
      secure: false
      # ftp connection information
      host: "192.168.0.1"
      port: 21
      # ftp login credentials
      username: admin
      password: password
      # remote directory
      # ! THIS DIRECTORY NEEDS TO BE DEDICATED TO THIS BACKUP PROFILE !
      # ! OTHERWISE ROTATION DONT WORK !
      path: backups
```
