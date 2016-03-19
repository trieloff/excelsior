#!/bin/sh
aws configure <<EOF
$AWS_ACCESS_KEY
$AWS_SECRET_KEY


EOF

VERSION=`xmllint ~/.m2/repository/excelsior/excelsior/0.1.0-SNAPSHOT/maven-metadata-private.xml --xpath "/metadata/versioning/snapshotVersions/snapshotVersion[1]/value/text()"`
echo "Latest version is $VERSION"

aws lambda update-function-code --region us-east-1 --function-name excelsior-dev --s3-bucket leinrepo --s3-key releases/excelsior/excelsior/0.1.0-SNAPSHOT/excelsior-$VERSION.jar