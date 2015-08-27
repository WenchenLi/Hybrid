# Hybrid
hybrid pic of two image from high frequency to low frequency, such that people can interpret the picture differently based on the size or distance from the picture.More on this technique please refer to [this](http://cvcl.mit.edu/hybrid/OlivaTorralb_Hybrid_Siggraph06.pdf).



Please add opencv to android studio according to [this] (http://stackoverflow.com/questions/27406303/opencv-in-android-studio), you can skip step 6 since jniLibs is already in the repo.

The below part is for my own reference in the future:
lambda * v  = c, where c is the speed of the light, lambda is the wavelength and v is the frequency of the lightwave. When sensor capture the image,it capture the complicated waves distribution of that moment through aperture. It's a discrete approximation of the natural light given the [Wave–particle duality}(https://en.wikipedia.org/wiki/Flicker_fusion_threshold) of light.

We have high pass filter and low pass filter to filter the image to high frequency part and low frequency part. 
in 2d, Gaussian filter is a good example of low pass filter, all the energy concentrate at the low frequency domain.
![low frequncy filter](https://www.clear.rice.edu/elec301/Projects01/image_filt/matlab/lpf_mesh.gif).

The opposite of Gaussian filter make an example of high frequncy filter like this one.![high frequncy filter](http://cs.brown.edu/courses/cs143/results/proj1/zyp/images/high.png)

I remember from the lecture James mention natural light is more in low frequency domain.


