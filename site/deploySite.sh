buildSite.sh
echo 'tarring site'
tar cf sodSite.tar generatedSite/*
echo 'copying site to pooh'
scp sodSite.tar pooh:.
echo done

