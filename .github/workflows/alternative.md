```yaml
# Alternative steps

#    steps:
#      - uses: actions/checkout@v2
#      - uses: actions/setup-java@v2
#        with:
#          distribution: 'adopt-openj9'
#          java-version: '1.8'
#          cache: 'gradle'
#      - run: ./gradlew build --full-stacktrace --no-daemon

# For run gradle to work you need to run this in your repo (and push the changes:
# git update-index --chmod=+x gradlew
```