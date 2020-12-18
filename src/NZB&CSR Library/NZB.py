#© 2020 Gihyun Park <texnee@gmail.com>

import torch

'''
커널을 bytearray의 nzb로 만들어주는 함수
인덱스(bytearray)와 데이터(list)를 반환한다.
'''
def convertNzb(kernel): # 3x3 single kernel
    data = [] #커널 실제 데이터
    index = bytearray() #변환된 13개 인덱스 값
    nonzeroNum = 0#nonzero의 수
    maskbit = [] #maskbit  
    indexTemp = [] #
    for i in range(3):
        for j in range(3):
            if(kernel[i][j] != 0):
                nonzeroNum += 1
                maskbit.append(True)
                data.append(float(kernel[i][j]))
            else:
                maskbit.append(False)
    indexTemp = list("{:04b}".format(nonzeroNum)) + maskbit
    for i in indexTemp:
        index.append(int(i))

    return index, data

'''
NZB전용 컨볼루션 함수
하나의 사진과 필터를 컨볼루션한다.
'''
def convolve2D_nzb(image, index, data, padding=1, strides=1): #한 개의 data와 kernel을 convolve한다.
    kernShape = 3
    imgShape = image.shape[0]

    output = int(((imgShape - kernShape + 2 * padding) / strides) + 1)
    output = torch.zeros(output, output).to('cuda')

    if padding != 0:
        imagePadded = torch.zeros(imgShape + padding*2, imgShape + padding*2).to('cuda')
        imagePadded[int(padding):int(-1 * padding), int(padding):int(-1 * padding)] = image
    else:
        imagePadded = image
    
    for i in range(imgShape): #가로
        for j in range(imgShape): #세로
            index_i = 4
            n = 0
            for ip in range(3): #행렬합
                for jp in range(3):
                    if(index[index_i] != 0):
                        output[i][j] += imagePadded[i+ip][j+jp]*data[n]
                        n += 1
                    index_i += 1
                      
    return output

'''
기존의 커널들이 있는 텐서들을 NZB 텐서로 만들어주는 Class
'''
class Nzbs:
    def __init__(self, weight):
        self.first = weight.shape[0]
        self.second = weight.shape[1]
        self.nzbWeight = bytearray()
        self.nzbData = []
        for i in range(self.first):
            for j in range(self.second):
                index, data = convertNzb(weight[i][j])
                self.nzbWeight += index
                self.nzbData.append(data)
'''
convolve2D_nzb 여러장을 convolution 하는 함수
'''
def Myconv_nzb(data, nzbs, bias):
    batchSize = data.shape[0]
    input = data.shape[1]
    dataSize = data.shape[2]
    output = nzbs.first
    outData = torch.zeros(batchSize, output, dataSize, dataSize).to('cuda') #Creat output data
    print("out:", outData.shape)
    for i in range(batchSize): #batchSize loop
        print(i,"/", batchSize)
        for j in range(output): # the number of output weight
            for k in range(input): #the number of kernel layer
                point = 13*nzbs.second*j + 13*k
                #print("point: ", point)
                weightTemp = nzbs.nzbWeight[point: point+13]
                #print(weightTemp)
                if(weightTemp[0] + weightTemp[1] + weightTemp[2] + weightTemp[3] != 0):
                    outData[i][j] += convolve2D_nzb(data[i][k], nzbs.nzbWeight[point:point+13], nzbs.nzbData[nzbs.second*j + k])
                
            outData[i][j] += bias[j]
            

    return outData

