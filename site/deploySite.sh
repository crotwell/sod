buildSite.sh
cd schemaDocs
buildSchemaDocs.py
cd ..
tar cjvf site.tar generatedSite/*
echo 'copying site to pooh'
scp -r site.tar sac@pooh:/seis/raid1/Apache/htdocs/SOD
echo done

