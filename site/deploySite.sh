buildSite.sh
cd generatedSite
tar cjvf site.tar.bz2 *
echo 'copying site to pooh'
scp -r site.tar.bz2 sac@pooh:/seis/raid1/Apache/htdocs/sod
ssh sac@pooh 'cd /seis/raid1/Apache/htdocs/sod ; bunzip2 site.tar.bz2 ; tar vxf site.tar; rm site.tar'
rm site.tar.bz2
echo done
