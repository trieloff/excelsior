# Excelsior
Turn Excel into HTTP


## Excelsior
*By Henry Wadsworth Longfellow*
````
The shades of night were falling fast,
As through an Alpine village passed
A youth, who bore, 'mid snow and ice,
A banner with the strange device,
      Excelsior!

His brow was sad; his eye beneath,
Flashed like a falchion from its sheath,
And like a silver clarion rung
The accents of that unknown tongue,
      Excelsior!

In happy homes he saw the light
Of household fires gleam warm and bright;
Above, the spectral glaciers shone,
And from his lips escaped a groan,
      Excelsior!

"Try not the Pass!" the old man said;
"Dark lowers the tempest overhead,
The roaring torrent is deep and wide!"
And loud that clarion voice replied,
      Excelsior!

"Oh stay," the maiden said, "and rest
Thy weary head upon this breast! "
A tear stood in his bright blue eye,
But still he answered, with a sigh,
      Excelsior!

"Beware the pine-tree's withered branch!
Beware the awful avalanche!"
This was the peasant's last Good-night,
A voice replied, far up the height,
      Excelsior!

At break of day, as heavenward
The pious monks of Saint Bernard
Uttered the oft-repeated prayer,
A voice cried through the startled air,
      Excelsior!

A traveller, by the faithful hound,
Half-buried in the snow was found,
Still grasping in his hand of ice
That banner with the strange device,
      Excelsior!

There in the twilight cold and gray,
Lifeless, but beautiful, he lay,
And from the sky, serene and far,
A voice fell like a falling star,
      Excelsior!
````

## Configuration

All configuration is performed through environment variables. Excelsior uses the [environ](https://github.com/weavejester/environ) library, so you can easily set up local development using [DynamoDB Local](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html) by adding the right environment variables to your `profiles.clj`.

* `AWS_ACCESS_KEY` – your AWS access key, required to access DynamoDB and S3
* `AWS_SECRET_KEY` – your AWS secret key, required to access DynamoDB and S3
* `DYNAMODB_ENDPOINT` – URL of the DynamoDB instance to use. For instance "https://dynamodb.eu-west-1.amazonaws.com" for the EU West [region](http://docs.aws.amazon.com/general/latest/gr/rande.html#ddb_region) or "http://localhost:8000" for a local development instance.The protocol must be included.
* `DYNAMODB_CRYPT_KEY – encryption password for encrypted data storage in DynamoDB

### Local Development

Add (and replace appropriately) in your local `profiles.clj`:

````
        :env {:aws_access_key "random"
              :aws_secret_key "random"
              :dynamodb_endpoint "http://localhost:8000"
              :dynamodb_crypt_key "secret"}
````

Run `lein ring server` for a local instance running on port 3000. You need to have the configuration set as above, and (optionally) have `dynamodb-local` running in parallel.
