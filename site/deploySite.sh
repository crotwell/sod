buildSite.sh
cd generatedSite
echo 'copying site to pooh'
scp -r * sac@pooh:/seis/raid1/Apache/htdocs/SOD
echo done

