import torch.nn as nn
from collections import OrderedDict


def convertCsr(kernel): # 3x3 single kernel
    data = [] #커널 실제 데이터
    row = []
    col = []
    rowData = 0
    for i in range(3):
        for j in range(3):
            if(kernel[i][j] != 0):
                rowData += 1
                col.append(j)
                #print(j)
                data.append(kernel[i][j])
        row.append(rowData)

    return row, col, data

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

def convolve2D_csr(image, row, col, data, padding=1, strides=1): #한 개의 data와 kernel을 convolve한다.
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
            n = 0 #data
            m = 0 #col
            dataNum = row[0]
            for k in range(dataNum):
                output[i][j] += imagePadded[i][j+col[m]]*data[n]
                n += 1
                m += 1
            dataNum = row[1]-row[0]
            for k in range(dataNum):
                output[i][j] += imagePadded[i+1][j+col[m]]*data[n]
                n += 1
                m += 1
            dataNum = row[2]-row[1]
            for k in range(dataNum):
                output[i][j] += imagePadded[i+2][j+col[m]]*data[n]
                n += 1
                m += 1
                      
    return output

def convolve2D(image, kernel, padding=1, strides=1): #한 개의 data와 kernel을 convolve한다.
    #print(image)
    #print(kernel)
    #print(kernel.shape, image.shape)
    kernShape = kernel.shape[0]
    imgShape = image.shape[0]


    #print(xKernShape, yKernShape, xImgShape, yImgShape)
    # Shape of Output Convolution
    output = int(((imgShape - kernShape + 2 * padding) / strides) + 1)
    output = torch.zeros(output, output).to('cuda')
    #print("oo")
    # Apply Equal Padding to All Sides
    if padding != 0:
        imagePadded = torch.zeros(imgShape + padding*2, imgShape + padding*2).to('cuda')
        imagePadded[int(padding):int(-1 * padding), int(padding):int(-1 * padding)] = image
        #print("imagePadded")
    else:
        imagePadded = image
   # print(imagePadded)
    #print(kernel)
    # Iterate through image
    #print(imagePadded[1][1], kernel[1][0])
    #print(imagePadded[1][1]*kernel[1][0])
    
    for i in range(imgShape): #가로
        for j in range(image.shape[0]): #세로
            output[i][j] = imagePadded[i][j]*kernel[0][0] + imagePadded[i][j+1]*kernel[0][1] + imagePadded[i][j+2]*kernel[0][2] + imagePadded[i+1][j]*kernel[1][0] + imagePadded[i+1][j+1]*kernel[1][1] + imagePadded[i+1][j+2]*kernel[1][2] + imagePadded[i+2][j]*kernel[2][0] + imagePadded[i+2][j+1]*kernel[2][1] + imagePadded[i+2][j+2]*kernel[2][2]
                        
    return output


def Myconv(data, weight, bias):
    batchSize = data.shape[0]
    input = data.shape[1]
    dataSize = data.shape[2]
    output = weight.shape[0]
    outData = torch.zeros(batchSize, output, dataSize, dataSize).to('cuda') #Creat output data
    print("out:", outData.shape)
    for i in range(batchSize): #batchSize loop
        print(i,"/", batchSize)
        outTemp = 0
        for j in range(output): # the number of output weight
            for k in range(input): #the number of kernel layer
                outData[i][j] += convolve2D(data[i][k], weight[j][k])
            outData[i][j] += bias[j]
            

    return outData


def Myconv_nzb(data, nzbs, bias):
    batchSize = data.shape[0]
    input = data.shape[1]
    dataSize = data.shape[2]
    output = nzbs.first
    outData = torch.zeros(batchSize, output, dataSize, dataSize).to('cuda')  # Creat output data
    print("out:", outData.shape)
    for i in range(batchSize):  # batchSize loop
        print(i, "/", batchSize)
        for j in range(output):  # the number of output weight
            for k in range(input):  # the number of kernel layer
                point = 13 * nzbs.second * j + 13 * k
                # print("point: ", point)
                weightTemp = nzbs.nzbWeight[point: point + 13]
                # print(weightTemp)
                if (weightTemp[0] + weightTemp[1] + weightTemp[2] + weightTemp[3] != 0):
                    outData[i][j] += convolve2D_nzb(data[i][k], nzbs.nzbWeight[point:point + 13],
                                                    nzbs.nzbData[nzbs.second * j + k])

            outData[i][j] += bias[j]

    return outData

def Myconv_csr(data, csrs, bias):
    batchSize = data.shape[0]
    input = data.shape[1]
    dataSize = data.shape[2]
    output = csrs.first
    outData = torch.zeros(batchSize, output, dataSize, dataSize).to('cuda') #Creat output data
    print("out:", outData.shape)
    for i in range(batchSize): #batchSize loop
        print(i,"/", batchSize)
        for j in range(output): # the number of output weight
            for k in range(input): #the number of kernel layer
                point = csrs.second*j + k
                
                outData[i][j] += convolve2D_csr(data[i][k], csrs.csrRows[point], csrs.csrCols[point], csrs.csrData[point])
                
            outData[i][j] += bias[j]
            

    return outData

class nzbs:
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

class csrs:
    def __init__(self, weight):
        self.first = weight.shape[0]
        self.second = weight.shape[1]
        self.csrRows = []
        self.csrCols = []
        self.csrData = []
        for i in range(self.first):
            for j in range(self.second):
                row, col, data = convertCsr(weight[i][j])
                self.csrRows.append(row)
                self.csrCols.append(col)
                self.csrData.append(data)


class BidirectionalLSTM(nn.Module):

    def __init__(self, nIn, nHidden, nOut):
        super(BidirectionalLSTM, self).__init__()

        self.rnn = nn.LSTM(nIn, nHidden, bidirectional=True)
        self.embedding = nn.Linear(nHidden * 2, nOut)

    def forward(self, input):
        recurrent, _ = self.rnn(input)
        T, b, h = recurrent.size()
        t_rec = recurrent.view(T * b, h)

        output = self.embedding(t_rec)  # [T * b, nOut]
        output = output.view(T, b, -1)
        return output


class CRNN(nn.Module):
    
    def __init__(self, imgH, nc, nclass, nh, leakyRelu=False):
        super(CRNN, self).__init__()
        assert imgH % 16 == 0, 'imgH has to be a multiple of 16'

        # 1x32x128
        self.conv1 = nn.Conv2d(nc, 64, 3, 1, 1)
        self.relu1 = nn.ReLU(True)
        self.pool1 = nn.MaxPool2d(2, 2)

        # 64x16x64
        self.conv2 = nn.Conv2d(64, 128, 3, 1, 1)
        self.relu2 = nn.ReLU(True)
        self.pool2 = nn.MaxPool2d(2, 2)

        # 128x8x32
        self.conv3_1 = nn.Conv2d(128, 256, 3, 1, 1)
        self.bn3 = nn.BatchNorm2d(256)
        self.relu3_1 = nn.ReLU(True)
        self.conv3_2 = nn.Conv2d(256, 256, 3, 1, 1)
        self.relu3_2 = nn.ReLU(True)
        self.pool3 = nn.MaxPool2d((2, 2), (2, 1), (0, 1))

        # 256x4x16
        self.conv4_1 = nn.Conv2d(256, 512, 3, 1, 1)
        self.bn4 = nn.BatchNorm2d(512)
        self.relu4_1 = nn.ReLU(True)
        self.conv4_2 = nn.Conv2d(512, 512, 3, 1, 1)
        self.relu4_2 = nn.ReLU(True)
        self.pool4 = nn.MaxPool2d((2, 2), (2, 1), (0, 1))

        # 512x2x16
        self.conv5 = nn.Conv2d(512, 512, 2, 1, 0)
        self.bn5 = nn.BatchNorm2d(512)
        self.relu5 = nn.ReLU(True)

        # 512x1x16

        self.rnn = nn.Sequential(
            BidirectionalLSTM(512, nh, nh),
            BidirectionalLSTM(nh, nh, nclass))

    def forward(self, input):
        
        if infer==1:    #nzb
            nzbs1 = nzbs(self.conv1.weight.cpu().detach().numpy())
            nzbs2 = nzbs(self.conv2.weight.cpu().detach().numpy())
            nzbs3_1 = nzbs(self.conv3_1.weight.cpu().detach().numpy())
            nzbs3_2 = nzbs(self.conv3_2.weight.cpu().detach().numpy())
            nzbs4_1 = nzbs(self.conv4_1.weight.cpu().detach().numpy())
            nzbs4_2 = nzbs(self.conv4_2.weight.cpu().detach().numpy())
            nzbs5 = nzbs(self.conv5.weight.cpu().detach().numpy())
            x = self.pool1(self.relu1(self.Myconv_nzb(x.cpu(), nzbs1, self.conv1.bias.cpu().detach().numpy()).to('cuda')))
            x = self.pool2(self.relu2(self.Myconv_nzb(x.cpu(), nzbs2, self.conv2.bias.cpu().detach().numpy()).to('cuda')))
            x = self.pool3(self.relu3_2(self.Myconv_nzb(self.relu3_1(self.bn3(self.Myconv_nzb(x.cpu(), nzbs3_1, self.conv3_1.bias.cpu().detach().numpy()).to('cuda'))).cpu(), nzbs3_2, self.conv3_2.bias.cpu().detach().numpy()).to('cuda')))
            x = self.pool4(self.relu4_2(self.Myconv_nzb(self.relu4_1(self.bn4(self.Myconv_nzb(x.cpu(), nzbs4_1, self.conv4_1.bias.cpu().detach().numpy()).to('cuda'))).cpu(), nzbs4_2, self.conv3_2.bias.cpu().detach().numpy()).to('cuda')))
            conv = self.relu5(self.bn5(self.Myconv_nzb(x.cpu(), nzbs5, self.conv5.bias.cpu().detach().numpy()).to('cuda')))
            
        elif infer==2:  #csr
            csrs1 = csrs(self.conv1.weight.cpu().detach().numpy())
            csrs2 = csrs(self.conv2.weight.cpu().detach().numpy())
            csrs3_1 = csrs(self.conv3_1.weight.cpu().detach().numpy())
            csrs3_2 = csrs(self.conv3_2.weight.cpu().detach().numpy())
            csrs4_1 = csrs(self.conv4_1.weight.cpu().detach().numpy())
            csrs4_2 = csrs(self.conv4_2.weight.cpu().detach().numpy())
            csrs5 = csrs(self.conv5.weight.cpu().detach().numpy())
            x = self.pool1(self.relu1(self.Myconv_csr(x.cpu(), csrs1, self.conv1.bias.cpu().detach().numpy()).to('cuda')))
            x = self.pool2(self.relu2(self.Myconv_csr(x.cpu(), csrs2, self.conv2.bias.cpu().detach().numpy()).to('cuda')))
            x = self.pool3(self.relu3_2(self.Myconv_csr(self.relu3_1(self.bn3(self.Myconv_csr(x.cpu(), csrs3_1, self.conv3_1.bias.cpu().detach().numpy()).to('cuda'))).cpu(), csrs3_2, self.conv3_2.bias.cpu().detach().numpy()).to('cuda')))
            x = self.pool4(self.relu4_2(self.Myconv_csr(self.relu4_1(self.bn4(self.Myconv_csr(x.cpu(), csrs4_1, self.conv4_1.bias.cpu().detach().numpy()).to('cuda'))).cpu(), csrs4_2, self.conv3_2.bias.cpu().detach().numpy()).to('cuda')))
            conv = self.relu5(self.bn5(self.Myconv_csr(x.cpu(), csrs5, self.conv5.bias.cpu().detach().numpy()).to('cuda')))

        else:   #defalut
            # conv features
        x = self.pool1(self.relu1(self.conv1(input)))
        x = self.pool2(self.relu2(self.conv2(x)))
        x = self.pool3(self.relu3_2(self.conv3_2(self.relu3_1(self.bn3(self.conv3_1(x))))))
        x = self.pool4(self.relu4_2(self.conv4_2(self.relu4_1(self.bn4(self.conv4_1(x))))))
        conv = self.relu5(self.bn5(self.conv5(x)))
            # print(conv.size())

        b, c, h, w = conv.size()
        assert h == 1, "the height of conv must be 1"
        conv = conv.squeeze(2)
        conv = conv.permute(2, 0, 1)  # [w, b, c]

        # rnn features
        output = self.rnn(conv)

        return output





def conv3x3(nIn, nOut, stride=1):
    # "3x3 convolution with padding"
    return nn.Conv2d( nIn, nOut, kernel_size=3, stride=stride, padding=1, bias=False )


class basic_res_block(nn.Module):

    def __init__(self, nIn, nOut, stride=1, downsample=None):
        super( basic_res_block, self ).__init__()
        m = OrderedDict()
        m['conv1'] = conv3x3( nIn, nOut, stride )
        m['bn1'] = nn.BatchNorm2d( nOut )
        m['relu1'] = nn.ReLU( inplace=True )
        m['conv2'] = conv3x3( nOut, nOut )
        m['bn2'] = nn.BatchNorm2d( nOut )
        self.group1 = nn.Sequential( m )

        self.relu = nn.Sequential( nn.ReLU( inplace=True ) )
        self.downsample = downsample

    def forward(self, x):
        if self.downsample is not None:
            residual = self.downsample( x )
        else:
            residual = x
        out = self.group1( x ) + residual
        out = self.relu( out )
        return out



if __name__ == '__main__':
    pass
