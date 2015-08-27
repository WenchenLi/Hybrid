# Hybrid
hybrid pic of two image from high frequency to low frequency, such that people can interpret the picture differently based on the size or distance from the picture.More on this technique please refer to [this](http://cvcl.mit.edu/hybrid/OlivaTorralb_Hybrid_Siggraph06.pdf).



Please add opencv to android studio according to [this] (http://stackoverflow.com/questions/27406303/opencv-in-android-studio), you can skip step 6 since jniLibs is already in the repo.

The below part is for my own reference in the future:
lambda * v  = c, where c is the speed of the light, lambda is the wavelength and v is the frequency of the lightwave. When sensor capture the image,it capture the complicated waves distribution of that moment through aperture. It's a discrete approximation of the natural light given the light.

We have high pass filter and low pass filter to filter the image to high frequency part and low frequency part. 
in 2d, Gaussian filter is a good example of low pass filter, all the energy concentrate at the low frequency domain.
![alt tag](https://www.google.com/url?sa=i&rct=j&q=&esrc=s&source=images&cd=&cad=rja&uact=8&ved=0CAcQjRxqFQoTCKyQm4-UyMcCFcMzPgodfDcAAg&url=https%3A%2F%2Fwww.clear.rice.edu%2Felec301%2FProjects01%2Fimage_filt%2Flow.html&ei=_m3eVayZN8Pn-AH87oAQ&psig=AFQjCNH0fGO-F8OIqNyqUQ5jcQii4Agivg&ust=1440726897038082)
